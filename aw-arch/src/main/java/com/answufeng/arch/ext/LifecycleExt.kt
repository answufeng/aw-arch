package com.answufeng.arch.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.event.FlowEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * LifecycleOwner 扩展：生命周期感知的事件观察与协程启动。
 *
 * [observeEvent]：观察 FlowEventBus；[sticky] 为 `true` 时订阅粘性通道。
 */
inline fun <reified T : Any> LifecycleOwner.observeEvent(
    sticky: Boolean = false,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            val flow =
                if (sticky) {
                    FlowEventBus.observeSticky<T>()
                } else {
                    FlowEventBus.observe<T>()
                }
            flow.collect { action(it) }
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
