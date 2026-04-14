package com.answufeng.arch.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.event.FlowEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectOnLifecycle(
    owner: LifecycleOwner,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(minState) {
            collect { value -> collector(value) }
        }
    }
}

fun LifecycleOwner.launchOnStarted(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

fun LifecycleOwner.launchOnResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED, block)
    }
}

inline fun <reified T : Any> LifecycleOwner.observeEvent(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit
) {
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
    lifecycleScope.launch {
        repeatOnLifecycle(minState) {
            FlowEventBus.observe<T>().collect { value -> collector(value) }
        }
    }
}

inline fun <reified T : Any> LifecycleOwner.observeStickyEvent(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit
) {
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
    lifecycleScope.launch {
        repeatOnLifecycle(minState) {
            FlowEventBus.observeSticky<T>().collect { value -> collector(value) }
        }
    }
}
