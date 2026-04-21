package com.answufeng.arch.ext

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 在 LifecycleOwner 的指定生命周期状态下收集 Flow。
 *
 * @param lifecycleOwner 生命周期拥有者
 * @param state          生命周期状态，默认 STARTED
 * @param action         收集回调
 */
fun <T : Any> Flow<T>.collectOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect { action(it) }
        }
    }
}

/**
 * 节流操作：在 [windowMillis] 时间窗口内只发射第一个事件。
 *
 * 适用于按钮点击防抖、搜索触发等场景。
 *
 * @param windowMillis 时间窗口（毫秒）
 */
fun <T> Flow<T>.throttleFirst(windowMillis: Long): Flow<T> {
    return ThrottleFirstFlow(this, windowMillis)
}

/**
 * 节流 Flow 实现：在 [windowMillis] 时间窗口内只发射第一个元素。
 *
 * 与 [debounce] 的区别：throttle 在窗口期开始时立即发射，debounce 在窗口期结束时发射。
 * 适用于按钮点击防抖、搜索触发等场景。
 *
 * @param T 元素类型
 * @param source 上游 Flow
 * @param windowMillis 节流窗口（毫秒）
 */
internal class ThrottleFirstFlow<T>(
    private val source: Flow<T>,
    private val windowMillis: Long
) : Flow<T> by source {
    override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<T>) {
        var lastTime = 0L
        source.collect { value ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastTime >= windowMillis) {
                lastTime = now
                collector.emit(value)
            }
        }
    }
}

/**
 * 防抖操作：在 [timeoutMillis] 时间内无新事件才发射最后一个。
 *
 * 适用于搜索输入框实时搜索等场景。
 *
 * @param timeoutMillis 超时时间（毫秒）
 */
fun <T> Flow<T>.debounceAction(timeoutMillis: Long): Flow<T> {
    return debounce(timeoutMillis)
}

/**
 * 从 StateFlow 中选择子字段，仅在字段值变化时发射。
 *
 * @param selector 字段选择器
 */
fun <T, R> StateFlow<T>.select(selector: (T) -> R): Flow<R> {
    return map(selector).distinctUntilChanged()
}

/**
 * 从 Flow 中选择子字段，仅在字段值变化时发射。
 *
 * @param selector 字段选择器
 */
fun <T, R> Flow<T>.select(selector: (T) -> R): Flow<R> {
    return map(selector).distinctUntilChanged()
}

/**
 * View 点击事件节流 Flow。在 [windowMillis] 时间窗口内只发射一次。
 *
 * @param windowMillis 时间窗口（毫秒），默认 300
 */
fun View.throttleClicks(windowMillis: Long = 300): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener { trySend(Unit) }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.throttleFirst(windowMillis)

/**
 * 观察 MVI 架构的 StateFlow 和事件 Flow，在指定生命周期状态下自动收集。
 *
 * @param stateFlow   UI 状态流
 * @param eventFlow   UI 事件流
 * @param state       生命周期状态，默认 STARTED
 * @param render      状态渲染回调
 * @param handleEvent 事件处理回调
 */
fun <S : UiState, E : UiEvent> LifecycleOwner.observeMvi(
    stateFlow: StateFlow<S>,
    eventFlow: Flow<E>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    render: (S) -> Unit,
    handleEvent: (E) -> Unit = {}
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            launch { stateFlow.collect { render(it) } }
            launch { eventFlow.collect { handleEvent(it) } }
        }
    }
}
