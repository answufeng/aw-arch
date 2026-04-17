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

object FlowEventBus {

    private val flows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val stickyFlows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun <T : Any> post(event: T) {
        scope.launch {
            getFlowInternal(event::class).emit(event)
        }
    }

    fun <T : Any> tryPost(event: T): Boolean {
        return getFlowInternal(event::class).tryEmit(event)
    }

    fun <T : Any> postSticky(event: T) {
        scope.launch {
            getStickyFlowInternal(event::class).emit(event)
        }
    }

    fun <T : Any> tryPostSticky(event: T): Boolean {
        return getStickyFlowInternal(event::class).tryEmit(event)
    }

    fun <T : Any> observe(clazz: KClass<T>): Flow<T> {
        return getFlowInternal(clazz).asSharedFlow() as Flow<T>
    }

    inline fun <reified T : Any> observe(): Flow<T> {
        return getFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    fun <T : Any> observeSticky(clazz: KClass<T>): Flow<T> {
        return getStickyFlowInternal(clazz).asSharedFlow() as Flow<T>
    }

    inline fun <reified T : Any> observeSticky(): Flow<T> {
        return getStickyFlowInternal(T::class).asSharedFlow() as Flow<T>
    }

    fun <T : Any> removeSticky(clazz: KClass<T>) {
        stickyFlows.remove(clazz)
    }

    inline fun <reified T : Any> removeSticky() {
        stickyFlows.remove(T::class)
    }

    @PublishedApi
    internal fun <T : Any> getFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return flows.getOrPut(clazz) { MutableSharedFlow(extraBufferCapacity = 1) }
    }

    @PublishedApi
    internal fun <T : Any> getStickyFlowInternal(clazz: KClass<T>): MutableSharedFlow<Any> {
        return stickyFlows.getOrPut(clazz) { MutableSharedFlow(extraBufferCapacity = 1, replay = 1) }
    }

    fun <T : Any> clear(clazz: KClass<T>) {
        flows.remove(clazz)
    }

    fun clearAll() {
        flows.clear()
        stickyFlows.clear()
    }
}
