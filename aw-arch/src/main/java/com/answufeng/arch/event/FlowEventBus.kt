package com.answufeng.arch.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 基于 SharedFlow 的事件总线，支持普通事件和粘性事件。
 *
 * - **普通事件**：通过 [post] / [tryPost] 发送，[observe] 订阅，仅新订阅者收到后续事件
 * - **粘性事件**：通过 [postSticky] / [tryPostSticky] 发送，[observeSticky] 订阅，新订阅者会立即收到最近一条
 * - **自动清理**：仅当某类型事件**曾经有过订阅者**且当前订阅数归零超过 [autoCleanupDelay] 毫秒后，
 *   才释放该类型对应的 SharedFlow。仅调用 [observe] / [observeSticky] 取得 Flow、尚未开始 collect 时**不会**触发清理。
 *   普通事件与粘性事件分别独立清理。
 *
 * ```kotlin
 * // 发送事件
 * FlowEventBus.post(LoginSuccessEvent)
 *
 * // 订阅事件
 * lifecycleScope.launch {
 *     FlowEventBus.observe<LoginSuccessEvent>().collect { event ->
 *         // 处理事件
 *     }
 * }
 * ```
 */
object FlowEventBus {

    private val flows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val stickyFlows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 普通 / 粘性通道各自持有清理任务，避免互相覆盖或误删另一类通道 */
    private data class CleanupKey(val clazz: KClass<*>, val sticky: Boolean)

    private val pendingCleanup = ConcurrentHashMap<CleanupKey, kotlinx.coroutines.Job>()

    /**
     * 自动清理延迟时间（毫秒）。当某类型事件无订阅者超过此时间后，自动释放资源。
     *
     * 设为 0 或负数可禁用自动清理。默认 30000ms（30秒）。
     *
     * 建议在 Application.onCreate() 中通过 [com.answufeng.arch.config.AwArch.init] 配置，
     * 避免运行时意外修改。
     */
    @Volatile
    var autoCleanupDelay: Long = 30_000L

    /**
     * 发送普通事件，挂起直到事件被投递。
     *
     * @param T 事件类型
     * @param event 事件实例
     */
    fun <T : Any> post(event: T) {
        scope.launch {
            getFlowInternal(event::class).emit(event)
        }
    }

    /**
     * 尝试发送普通事件，不挂起。
     *
     * @param T 事件类型
     * @param event 事件实例
     * @return 发送成功返回 `true`，缓冲区满时返回 `false`
     */
    fun <T : Any> tryPost(event: T): Boolean {
        return getFlowInternal(event::class).tryEmit(event)
    }

    /**
     * 发送粘性事件，挂起直到事件被投递。新订阅者会立即收到最近一条粘性事件。
     *
     * @param T 事件类型
     * @param event 事件实例
     */
    fun <T : Any> postSticky(event: T) {
        scope.launch {
            getStickyFlowInternal(event::class).emit(event)
        }
    }

    /**
     * 尝试发送粘性事件，不挂起。新订阅者会立即收到最近一条粘性事件。
     *
     * @param T 事件类型
     * @param event 事件实例
     * @return 发送成功返回 `true`，缓冲区满时返回 `false`
     */
    fun <T : Any> tryPostSticky(event: T): Boolean {
        return getStickyFlowInternal(event::class).tryEmit(event)
    }

    /**
     * 订阅普通事件，返回 [Flow] 供收集。仅收到订阅后发送的事件。
     *
     * @param T 事件类型
     * @param clazz 事件类型的 [KClass]
     * @return 事件 Flow
     */
    fun <T : Any> observe(clazz: KClass<T>): Flow<T> {
        cancelPendingCleanup(clazz, sticky = false)
        val flow = getFlowInternal(clazz)
        scheduleAutoCleanup(clazz, flow, sticky = false)
        return flow.asSharedFlow() as Flow<T>
    }

    /**
     * 订阅普通事件（reified 便捷版）。
     *
     * @param T 事件类型
     * @return 事件 Flow
     */
    inline fun <reified T : Any> observe(): Flow<T> {
        return observe(T::class)
    }

    /**
     * 订阅粘性事件，返回 [Flow] 供收集。新订阅者会立即收到最近一条粘性事件。
     *
     * @param T 事件类型
     * @param clazz 事件类型的 [KClass]
     * @return 事件 Flow
     */
    fun <T : Any> observeSticky(clazz: KClass<T>): Flow<T> {
        cancelPendingCleanup(clazz, sticky = true)
        val flow = getStickyFlowInternal(clazz)
        scheduleAutoCleanup(clazz, flow, sticky = true)
        return flow.asSharedFlow() as Flow<T>
    }

    /**
     * 订阅粘性事件（reified 便捷版）。
     *
     * @param T 事件类型
     * @return 事件 Flow
     */
    inline fun <reified T : Any> observeSticky(): Flow<T> {
        return observeSticky(T::class)
    }

    /**
     * 移除指定类型的粘性事件。
     *
     * @param T 事件类型
     * @param clazz 事件类型的 [KClass]
     */
    fun <T : Any> removeSticky(clazz: KClass<T>) {
        stickyFlows.remove(clazz)
    }

    /**
     * 移除指定类型的粘性事件（reified 便捷版）。
     */
    inline fun <reified T : Any> removeSticky() {
        removeStickyInternal(T::class)
    }

    @PublishedApi
    internal fun removeStickyInternal(clazz: KClass<*>) {
        stickyFlows.remove(clazz)
    }

    @PublishedApi
    internal fun <T : Any> getFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return flows.computeIfAbsent(clazz) { MutableSharedFlow(extraBufferCapacity = 1) }
    }

    @PublishedApi
    internal fun <T : Any> getStickyFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return stickyFlows.computeIfAbsent(clazz) { MutableSharedFlow(extraBufferCapacity = 1, replay = 1) }
    }

    /**
     * 清除指定类型的所有事件（普通 + 粘性）及自动清理任务。
     *
     * @param T 事件类型
     * @param clazz 事件类型的 [KClass]
     */
    fun <T : Any> clear(clazz: KClass<T>) {
        flows.remove(clazz)
        stickyFlows.remove(clazz)
        cancelPendingCleanup(clazz, sticky = false)
        cancelPendingCleanup(clazz, sticky = true)
    }

    /**
     * 清除所有类型的事件和自动清理任务。
     */
    fun clearAll() {
        flows.clear()
        stickyFlows.clear()
        pendingCleanup.values.forEach { it.cancel() }
        pendingCleanup.clear()
    }

    private fun cancelPendingCleanup(clazz: KClass<*>, sticky: Boolean) {
        pendingCleanup.remove(CleanupKey(clazz, sticky))?.cancel()
    }

    private fun scheduleAutoCleanup(clazz: KClass<*>, flow: MutableSharedFlow<*>, sticky: Boolean) {
        if (autoCleanupDelay <= 0) return
        cancelPendingCleanup(clazz, sticky)
        val key = CleanupKey(clazz, sticky)
        val job = scope.launch {
            var hadSubscribers = false
            flow.subscriptionCount.collect { count ->
                    if (count > 0) hadSubscribers = true
                    if (count == 0 && hadSubscribers) {
                        delay(autoCleanupDelay)
                        if (flow.subscriptionCount.value == 0) {
                            val removed = if (sticky) {
                                stickyFlows.remove(clazz, flow)
                            } else {
                                flows.remove(clazz, flow)
                            }
                            if (removed) {
                                pendingCleanup.remove(key)
                                coroutineContext[Job]?.cancel()
                            }
                        }
                    }
                }
        }
        pendingCleanup[key] = job
    }

    /** 单元测试用：是否仍存在普通事件通道 */
    internal fun containsNormalChannel(clazz: KClass<*>): Boolean = flows.containsKey(clazz)

    /** 单元测试用：是否仍存在粘性事件通道 */
    internal fun containsStickyChannel(clazz: KClass<*>): Boolean = stickyFlows.containsKey(clazz)
}
