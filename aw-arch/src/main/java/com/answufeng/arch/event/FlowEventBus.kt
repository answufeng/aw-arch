package com.answufeng.arch.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 基于 SharedFlow 的全局事件总线，支持类型安全观察和粘性事件。
 *
 * ```kotlin
 * // 定义事件
 * data class LoginSuccessEvent(val userId: String)
 *
 * // 发送事件
 * FlowEventBus.post(LoginSuccessEvent("user123"))
 *
 * // 观察事件（推荐使用 LifecycleOwner 扩展）
 * observeEvent<LoginSuccessEvent> { event ->
 *     updateUI(event.userId)
 * }
 * ```
 *
 * 线程安全：内部使用 [ConcurrentHashMap] 和 [computeIfAbsent] 确保并发安全。
 *
 * 调度器：[post] 使用 [Dispatchers.Default] 发射事件，
 * 接收者可通过 [flowOn] 切换到所需调度器。
 */
object FlowEventBus {

    private val flows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val stickyFlows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 挂起方式发送事件，保证事件一定被发射 */
    fun <T : Any> post(event: T) {
        scope.launch {
            getFlowInternal(event::class).emit(event)
        }
    }

    /** 非挂起方式发送事件，缓冲区满时返回 false */
    fun <T : Any> tryPost(event: T): Boolean {
        return getFlowInternal(event::class).tryEmit(event)
    }

    /** 挂起方式发送粘性事件，新订阅者会收到最近一次事件 */
    fun <T : Any> postSticky(event: T) {
        scope.launch {
            getStickyFlowInternal(event::class).emit(event)
        }
    }

    /** 非挂起方式发送粘性事件 */
    fun <T : Any> tryPostSticky(event: T): Boolean {
        return getStickyFlowInternal(event::class).tryEmit(event)
    }

    /** 按类型观察事件流 */
    fun <T : Any> observe(clazz: KClass<T>): Flow<T> {
        return getFlowInternal(clazz).asSharedFlow() as Flow<T>
    }

    /** 按泛型类型观察事件流（推荐用法） */
    inline fun <reified T : Any> observe(): Flow<T> {
        return getFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    /** 按类型观察粘性事件流，会重放最近一次事件 */
    fun <T : Any> observeSticky(clazz: KClass<T>): Flow<T> {
        return getStickyFlowInternal(clazz).asSharedFlow() as Flow<T>
    }

    /** 按泛型类型观察粘性事件流 */
    inline fun <reified T : Any> observeSticky(): Flow<T> {
        return getStickyFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    /** 移除指定类型的粘性事件 */
    fun <T : Any> removeSticky(clazz: KClass<T>) {
        stickyFlows.remove(clazz)
    }

    /** 移除指定泛型类型的粘性事件 */
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

    /** 清除指定类型的普通事件通道 */
    fun <T : Any> clear(clazz: KClass<T>) {
        flows.remove(clazz)
    }

    /** 清除所有事件通道 */
    fun clearAll() {
        flows.clear()
        stickyFlows.clear()
    }
}
