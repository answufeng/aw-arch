package com.answufeng.arch.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

/**
 * 基于 Flow 的事件总线。
 *
 * 用于组件间（如 Fragment 与 Fragment 之间）的通信，避免直接持有引用。
 *
 * ### 发送事件
 * ```kotlin
 * FlowEventBus.post(DemoEvent("Hello"))
 * ```
 *
 * ### 订阅事件
 * ```kotlin
 * FlowEventBus.observe<DemoEvent>().collect { event ->
 *     // 处理事件
 * }
 * ```
 *
 * ### 带生命周期感知的订阅
 * ```kotlin
 * FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
 *     // 处理事件
 * }
 * ```
 */
object FlowEventBus {

    private val flows = mutableMapOf<KClass<*>, MutableSharedFlow<Any>>()
    private val stickyFlows = mutableMapOf<KClass<*>, MutableSharedFlow<Any>>()

    /**
     * 发送事件到指定通道。
     *
     * @param event 事件数据
     */
    fun <T : Any> post(event: T) {
        CoroutineScope(Dispatchers.Default).launch {
            getFlow(event::class).emit(event)
        }
    }

    /**
     * 尝试同步发送事件。
     *
     * @param event 事件数据
     * @return 是否发送成功
     */
    fun <T : Any> tryPost(event: T): Boolean {
        return try {
            runBlocking(Dispatchers.Default) {
                getFlow(event::class).emit(event)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 发送粘性事件。
     *
     * @param event 事件数据
     */
    fun <T : Any> postSticky(event: T) {
        CoroutineScope(Dispatchers.Default).launch {
            getStickyFlow(event::class).emit(event)
        }
    }

    /**
     * 订阅指定类型的事件。
     *
     * @param clazz 事件类型
     * @return 事件流，需要在协程中 collect
     */
    fun <T : Any> observe(clazz: KClass<T>): Flow<T> {
        return getFlow(clazz).asSharedFlow() as Flow<T>
    }

    /**
     * 订阅指定类型的事件（使用泛型参数）。
     *
     * @return 事件流，需要在协程中 collect
     */
    inline fun <reified T : Any> observe(): Flow<T> {
        return getFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    /**
     * 观察粘性事件。
     *
     * @param clazz 事件类型
     * @return 事件流
     */
    fun <T : Any> observeSticky(clazz: KClass<T>): Flow<T> {
        return getStickyFlow(clazz).asSharedFlow() as Flow<T>
    }

    /**
     * 观察粘性事件（使用泛型参数）。
     *
     * @return 事件流
     */
    inline fun <reified T : Any> observeSticky(): Flow<T> {
        return getStickyFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    /**
     * 获取或创建事件流。
     */
    private fun <T : Any> getFlow(clazz: KClass<T>): MutableSharedFlow<Any> {
        return getFlowInternal(clazz)
    }

    /**
     * 获取或创建事件流（内部使用）。
     */
    @PublishedApi
    internal fun <T : Any> getFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return flows.getOrPut(clazz) { MutableSharedFlow(extraBufferCapacity = 1) } as MutableSharedFlow<Any>
    }

    /**
     * 获取或创建粘性事件流。
     */
    private fun <T : Any> getStickyFlow(clazz: KClass<T>): MutableSharedFlow<Any> {
        return getStickyFlowInternal(clazz)
    }

    /**
     * 获取或创建粘性事件流（内部使用）。
     */
    @PublishedApi
    internal fun <T : Any> getStickyFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return stickyFlows.getOrPut(clazz) { MutableSharedFlow(extraBufferCapacity = 1, replay = 1) } as MutableSharedFlow<Any>
    }

    /**
     * 清除指定类型的事件流。
     */
    fun <T : Any> clear(clazz: KClass<T>) {
        flows.remove(clazz)
    }

    /**
     * 清除所有事件流。
     */
    fun clearAll() {
        flows.clear()
        stickyFlows.clear()
    }
}