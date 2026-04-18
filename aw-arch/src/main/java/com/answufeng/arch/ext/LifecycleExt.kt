package com.answufeng.arch.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.event.FlowEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * LifecycleOwner 扩展函数，提供生命周期感知的事件观察和协程启动。
 */

/** 观察 FlowEventBus 中的普通事件，生命周期到达 [state] 时自动收集 */
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

/** 按类型观察普通事件（KClass 版本） */
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

/** 观察 FlowEventBus 中的粘性事件，新订阅者会收到最近一次事件 */
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

/** 按类型观察粘性事件（KClass 版本） */
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

/** 在 STARTED 状态启动协程，离开 STARTED 自动取消 */
fun LifecycleOwner.launchOnStarted(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block(this)
        }
    }
}

/** 在 RESUMED 状态启动协程，离开 RESUMED 自动取消 */
fun LifecycleOwner.launchOnResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block(this)
        }
    }
}
