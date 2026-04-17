package com.answufeng.arch.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.event.FlowEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

inline fun <reified T : Any> LifecycleOwner.observeEvent(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            FlowEventBus.observe<T>().collect { action(it) }
        }
    }
}

fun <T : Any> LifecycleOwner.observeEvent(
    clazz: KClass<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            FlowEventBus.observe(clazz).collect { action(it) }
        }
    }
}

inline fun <reified T : Any> LifecycleOwner.observeStickyEvent(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            FlowEventBus.observeSticky<T>().collect { action(it) }
        }
    }
}

fun <T : Any> LifecycleOwner.observeStickyEvent(
    clazz: KClass<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            FlowEventBus.observeSticky(clazz).collect { action(it) }
        }
    }
}

fun LifecycleOwner.launchOnStarted(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block(this)
        }
    }
}

fun LifecycleOwner.launchOnResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block(this)
        }
    }
}
