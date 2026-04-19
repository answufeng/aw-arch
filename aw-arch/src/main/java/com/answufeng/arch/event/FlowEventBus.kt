package com.answufeng.arch.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object FlowEventBus {

    private val flows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val stickyFlows = ConcurrentHashMap<KClass<*>, MutableSharedFlow<Any>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val pendingCleanup = ConcurrentHashMap<KClass<*>, kotlinx.coroutines.Job>()

    var autoCleanupDelay: Long = 30_000L

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
        cancelPendingCleanup(clazz)
        val flow = getFlowInternal(clazz)
        scheduleAutoCleanup(clazz, flow)
        return flow.asSharedFlow() as Flow<T>
    }

    inline fun <reified T : Any> observe(): Flow<T> {
        return observe(T::class)
    }

    fun <T : Any> observeSticky(clazz: KClass<T>): Flow<T> {
        cancelPendingCleanup(clazz)
        val flow = getStickyFlowInternal(clazz)
        scheduleAutoCleanup(clazz, flow)
        return flow.asSharedFlow() as Flow<T>
    }

    inline fun <reified T : Any> observeSticky(): Flow<T> {
        return observeSticky(T::class)
    }

    fun <T : Any> removeSticky(clazz: KClass<T>) {
        stickyFlows.remove(clazz)
    }

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

    fun <T : Any> clear(clazz: KClass<T>) {
        flows.remove(clazz)
        stickyFlows.remove(clazz)
        pendingCleanup.remove(clazz)?.cancel()
    }

    fun clearAll() {
        flows.clear()
        stickyFlows.clear()
        pendingCleanup.values.forEach { it.cancel() }
        pendingCleanup.clear()
    }

    private fun cancelPendingCleanup(clazz: KClass<*>) {
        pendingCleanup.remove(clazz)?.cancel()
    }

    private fun scheduleAutoCleanup(clazz: KClass<*>, flow: MutableSharedFlow<*>) {
        if (autoCleanupDelay <= 0) return
        cancelPendingCleanup(clazz)
        val job = scope.launch {
            flow.subscriptionCount
                .distinctUntilChanged()
                .collect { count ->
                    if (count == 0) {
                        delay(autoCleanupDelay)
                        if (flow.subscriptionCount.value == 0) {
                            flows.remove(clazz)
                            stickyFlows.remove(clazz)
                            pendingCleanup.remove(clazz)
                        }
                    }
                }
        }
        pendingCleanup[clazz] = job
    }
}
