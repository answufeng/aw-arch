package com.answufeng.arch.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.event.FlowEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 安全收集 Flow（绑定生命周期，默认 STARTED 时收集）。
 *
 * ```kotlin
 * viewModel.state.collectOnLifecycle(viewLifecycleOwner) { state ->
 *     render(state)
 * }
 * ```
 */
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

/**
 * 在 STARTED 状态安全启动协程
 */
fun LifecycleOwner.launchOnStarted(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

/**
 * 在 RESUMED 状态安全启动协程
 */
fun LifecycleOwner.launchOnResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED, block)
    }
}

// ==================== FlowEventBus 生命周期安全扩展 ====================

/**
 * 在 [LifecycleOwner] 上安全观察 FlowEventBus 事件。
 *
 * ```kotlin
 * observeEvent<LoginSuccessEvent> { event ->
 *     refreshUI(event.userId)
 * }
 * ```
 *
 * @param T 事件类型
 * @param minState 最小活跃状态，默认 [Lifecycle.State.STARTED]
 * @param collector 事件回调
 */
inline fun <reified T : Any> LifecycleOwner.observeEvent(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(minState) {
            FlowEventBus.observe<T>().collect { value -> collector(value) }
        }
    }
}

/**
 * 在 [LifecycleOwner] 上安全观察 FlowEventBus 粘性事件。
 *
 * ```kotlin
 * observeStickyEvent<ThemeChangedEvent> { event ->
 *     applyTheme(event.darkMode)
 * }
 * ```
 *
 * @param T 事件类型
 * @param minState 最小活跃状态，默认 [Lifecycle.State.STARTED]
 * @param collector 事件回调
 */
inline fun <reified T : Any> LifecycleOwner.observeStickyEvent(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(minState) {
            FlowEventBus.observeSticky<T>().collect { value -> collector(value) }
        }
    }
}
