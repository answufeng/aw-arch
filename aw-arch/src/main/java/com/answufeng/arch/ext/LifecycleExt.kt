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

/** @deprecated 请使用 [observeEvent]（`sticky` 参数）。 */
@Deprecated(
    message = "Use observeEvent(clazz, sticky = false, state, action)",
    replaceWith =
        ReplaceWith(
            "observeEvent(clazz, sticky = false, state, action)",
            "com.answufeng.arch.ext.LifecycleOwnerKt",
        ),
)
fun <T : Any> LifecycleOwner.observeEvent(
    clazz: KClass<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            FlowEventBus.observe(clazz).collect { action(it) }
        }
    }
}

/**
 * 观察 FlowEventBus 粘性事件。
 *
 * @deprecated 请使用 [observeEvent]（`sticky = true`）。
 */
@Deprecated(
    message = "Use observeEvent<T>(sticky = true, state, action)",
    replaceWith =
        ReplaceWith(
            "observeEvent<T>(sticky = true, state = state, action = action)",
        ),
)
inline fun <reified T : Any> LifecycleOwner.observeStickyEvent(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit,
) {
    observeEvent<T>(sticky = true, state = state, action = action)
}

/** @deprecated 请使用 [observeEvent]（`sticky = true`）。 */
@Deprecated(
    message = "Use observeEvent(clazz, sticky = true, state, action)",
    replaceWith =
        ReplaceWith(
            "observeEvent(clazz, sticky = true, state, action)",
            "com.answufeng.arch.ext.LifecycleOwnerKt",
        ),
)
fun <T : Any> LifecycleOwner.observeStickyEvent(
    clazz: KClass<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
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
