package com.answufeng.arch.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于 [SharedFlow] 的轻量事件总线，替代 EventBus / LiveDataBus。
 *
 * 支持普通事件和粘性事件，协程安全，配合 `repeatOnLifecycle` 自动感知生命周期。
 *
 * ### 线程安全
 * - 内部使用 [ConcurrentHashMap] 管理事件通道，[post] / [observe] 可在任意线程调用。
 * - [post] 和 [postSticky] 是 `suspend` 函数，背压时会挂起（缓冲区默认 16 条）。
 * - [tryPost] 和 [tryPostSticky] 是非挂起函数，缓冲区满时丢弃事件。
 *
 * ### 类型安全
 * - [observe] 和 [observeSticky] 返回类型安全的 [Flow]，
 *   自动过滤并转换为目标类型，无需手动 cast。
 * - 底层仍使用 `Any` 存储以支持多事件类型共享通道，
 *   但对外 API 保证类型安全。
 *
 * ### 生命周期要求
 * - **必须**在 `repeatOnLifecycle(STARTED)` 或等价作用域中 collect，
 *   否则 Activity/Fragment 销毁后协程不会自动取消，导致内存泄漏。
 * - 粘性事件通道持有最后一条消息引用，如需释放请调用 [removeSticky]。
 *
 * ### 发送事件
 * ```kotlin
 * viewModelScope.launch {
 *     FlowEventBus.post(LoginSuccessEvent("user123"))
 * }
 * FlowEventBus.tryPost(LoginSuccessEvent("user123"))
 * ```
 *
 * ### 接收事件
 * ```kotlin
 * FlowEventBus.observe<LoginSuccessEvent>().collectOnLifecycle(this) { event ->
 *     updateUI(event.userId)
 * }
 * ```
 *
 * ### 粘性事件（新订阅者自动收到最近一条）
 * ```kotlin
 * FlowEventBus.postSticky(ThemeChangedEvent(darkMode = true))
 * FlowEventBus.observeSticky<ThemeChangedEvent>().collect { ... }
 * ```
 */
object FlowEventBus {

    private val flows = ConcurrentHashMap<String, MutableSharedFlow<Any>>()
    private val stickyFlows = ConcurrentHashMap<String, MutableSharedFlow<Any>>()

    /**
     * 发送普通事件（挂起函数）。
     *
     * @param event 事件对象
     * @param key   事件通道 key，默认为事件类名
     */
    suspend fun post(event: Any, key: String = event.javaClass.name) {
        getFlow(key).emit(event)
    }

    /**
     * 发送普通事件（非挂起函数）。
     *
     * @param event 事件对象
     * @param key   事件通道 key，默认为事件类名
     * @return true 发送成功，false 缓冲区满被丢弃
     */
    fun tryPost(event: Any, key: String = event.javaClass.name): Boolean {
        return getFlow(key).tryEmit(event)
    }

    /**
     * 发送粘性事件（挂起函数）。
     *
     * @param event 事件对象
     * @param key   事件通道 key
     */
    suspend fun postSticky(event: Any, key: String = event.javaClass.name) {
        getStickyFlow(key).emit(event)
    }

    /**
     * 发送粘性事件（非挂起函数）。
     *
     * @param event 事件对象
     * @param key   事件通道 key
     * @return true 发送成功，false 缓冲区满被丢弃
     */
    fun tryPostSticky(event: Any, key: String = event.javaClass.name): Boolean {
        return getStickyFlow(key).tryEmit(event)
    }

    /**
     * 观察类型安全的普通事件流。
     *
     * ```kotlin
     * FlowEventBus.observe<LoginSuccessEvent>().collect { event ->
     *     updateUI(event.userId)
     * }
     * ```
     *
     * @param T  事件类型
     * @param key 事件通道 key，默认为 [T] 的类名
     */
    inline fun <reified T : Any> observe(key: String = T::class.java.name): Flow<T> =
        getFlow(key).asSharedFlow().filterIsInstance<T>()

    /**
     * 观察 Class 类型安全的普通事件流（非 inline 版本）。
     *
     * @param T  事件类型
     * @param eventType 事件的 Java Class 对象
     * @param key 事件通道 key，默认为 [eventType] 的类名
     */
    fun <T : Any> observe(eventType: Class<T>, key: String = eventType.name): Flow<T> =
        getFlow(key).asSharedFlow().filter { eventType.isInstance(it) }.map { eventType.cast(it)!! }

    /**
     * 观察类型安全的粘性事件流。
     *
     * @param T  事件类型
     * @param key 事件通道 key
     */
    inline fun <reified T : Any> observeSticky(key: String = T::class.java.name): Flow<T> =
        getStickyFlow(key).asSharedFlow().filterIsInstance<T>()

    /**
     * 观察 Class 类型安全的粘性事件流（非 inline 版本）。
     *
     * @param T  事件类型
     * @param eventType 事件的 Java Class 对象
     * @param key 事件通道 key
     */
    fun <T : Any> observeSticky(eventType: Class<T>, key: String = eventType.name): Flow<T> =
        getStickyFlow(key).asSharedFlow().filter { eventType.isInstance(it) }.map { eventType.cast(it)!! }

    /**
     * 观察原始普通事件流（返回 `SharedFlow<Any>`）。
     */
    fun observeRaw(key: String): SharedFlow<Any> =
        getFlow(key).asSharedFlow()

    /**
     * 观察原始粘性事件流（返回 `SharedFlow<Any>`）。
     */
    fun observeStickyRaw(key: String): SharedFlow<Any> =
        getStickyFlow(key).asSharedFlow()

    /** 移除指定 key 的粘性事件通道 */
    fun removeSticky(key: String) {
        stickyFlows.remove(key)
    }

    /**
     * 清除所有事件通道（普通 + 粘性）。
     *
     * **警告**：已通过 [observe] / [observeSticky] 获取引用的订阅者不受影响，
     * 后续调用 [observe] 会得到全新的 Flow 实例，与旧订阅者互相隔离。
     * 仅应在应用退出或完全重置时调用。
     */
    fun clear() {
        flows.clear()
        stickyFlows.clear()
    }

    @PublishedApi
    internal fun getFlow(key: String): MutableSharedFlow<Any> {
        return flows.computeIfAbsent(key) {
            MutableSharedFlow(extraBufferCapacity = 16)
        }
    }

    @PublishedApi
    internal fun getStickyFlow(key: String): MutableSharedFlow<Any> {
        return stickyFlows.computeIfAbsent(key) {
            MutableSharedFlow(replay = 1, extraBufferCapacity = 16)
        }
    }
}
