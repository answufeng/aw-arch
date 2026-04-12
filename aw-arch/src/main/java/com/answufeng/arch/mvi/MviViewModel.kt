package com.answufeng.arch.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.answufeng.arch.base.BaseViewModel
import com.answufeng.arch.config.BrickArch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * MVI 模式的 ViewModel 基类。
 *
 * 封装 State / Event / Intent 三层模型：
 * - **State**：页面完整状态，通过 [state] 暴露给 UI 层，每次变更触发 render
 * - **Event**：一次性副作用（导航、Toast 等），通过 [event] 暴露，消费后不重放
 * - **Intent**：用户意图，通过 [dispatch] 进入 ViewModel，由 [handleIntent] 处理
 *
 * ### 状态更新
 * ```kotlin
 * updateState { copy(count = count + 1) }
 * ```
 *
 * ### 发送一次性事件
 * ```kotlin
 * sendMviEvent(CounterEvent.ShowSnackbar("Added!"))
 * ```
 *
 * ### 节流分发（防连点）
 * ```kotlin
 * dispatchThrottled(CounterIntent.Increment, windowMillis = 500)
 * ```
 *
 * ### 异常处理
 * - 协程中的未捕获异常会回调 [handleException]
 * - 默认行为：记录日志（不弹 Toast，避免 MVI 场景下重复处理）
 * - 子类可覆写 [handleException] 统一错误上报
 *
 * @param S 页面状态类型，必须实现 [UiState]
 * @param E 一次性事件类型，必须实现 [UiEvent]
 * @param I 用户意图类型，必须实现 [UiIntent]
 * @param initialState 初始状态
 * @param savedStateHandle 进程恢复时保存的状态
 */
abstract class MviViewModel<S : UiState, E : UiEvent, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val _state = MutableStateFlow(initialState)

    /** 页面状态流，UI 层在 STARTED 状态 collect */
    val state: StateFlow<S> = _state.asStateFlow()

    /** 当前状态快照，适合在 ViewModel 内部读取 */
    protected val currentState: S get() = _state.value

    private val _event = Channel<E>(Channel.BUFFERED)

    /** 一次性事件流，UI 层在 STARTED 状态 collect */
    val event = _event.receiveAsFlow()

    private val intentThrottleMap = ConcurrentHashMap<String, Long>()

    /**
     * 处理用户意图。
     *
     * 子类在此方法中根据 Intent 类型更新 State 或发送 Event：
     * ```kotlin
     * override fun handleIntent(intent: HomeIntent) {
     *     when (intent) {
     *         HomeIntent.Refresh -> launchIO { refreshData() }
     *         is HomeIntent.LoadMore -> loadMore(intent.page)
     *     }
     * }
     * ```
     */
    abstract fun handleIntent(intent: I)

    /**
     * 分发用户意图到 [handleIntent]。
     *
     * 此方法可在任意线程调用。
     */
    fun dispatch(intent: I) {
        handleIntent(intent)
    }

    /**
     * 带节流的 Intent 分发，在 [windowMillis] 时间窗口内同类型 Intent 只处理第一个。
     *
     * 适用于按钮防连点、快速滑动等场景。节流 key 为 Intent 类名，
     * 不同类型的 Intent 互不影响。
     *
     * ```kotlin
     * // 防止 500ms 内重复点击提交
     * dispatchThrottled(SubmitIntent.Click, windowMillis = 500)
     * ```
     *
     * @param intent 用户意图
     * @param windowMillis 节流窗口（毫秒），默认 300ms
     */
    fun dispatchThrottled(intent: I, windowMillis: Long = 300) {
        val key = intent::class.java.name
        val now = System.nanoTime() / 1_000_000L
        val last = intentThrottleMap[key] ?: 0L
        if (now - last >= windowMillis) {
            intentThrottleMap[key] = now
            handleIntent(intent)
        }
    }

    /**
     * 清除节流缓存。
     *
     * [intentThrottleMap] 会随 Intent 类型增加而增长，
     * 如果 Intent 类型非常多，可在适当时机调用此方法释放内存。
     * 通常不需要手动调用，[onCleared] 时会自动清理。
     */
    protected fun clearThrottleCache() {
        intentThrottleMap.clear()
    }

    /**
     * 更新页面状态。
     *
     * 使用 [kotlinx.coroutines.flow.update] 保证原子性，
     * 多个并发 updateState 调用不会丢失更新。
     *
     * ```kotlin
     * updateState { copy(isLoading = true) }
     * ```
     */
    protected fun updateState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    /**
     * 发送一次性 MVI 事件。
     *
     * 事件通过 [Channel] 传递，保证一次性消费，不会在配置变更后重放。
     *
     * ```kotlin
     * sendMviEvent(HomeEvent.NavigateToDetail(itemId))
     * ```
     */
    protected fun sendMviEvent(event: E) {
        viewModelScope.launch { _event.send(event) }
    }

    override fun onCleared() {
        super.onCleared()
        intentThrottleMap.clear()
    }

    override fun handleException(throwable: Throwable) {
        BrickArch.logger.e("MviViewModel", "Unhandled exception in ${this::class.simpleName}", throwable)
    }

    final override fun sendEvent(event: com.answufeng.arch.base.BaseViewModel.UIEvent) {
        BrickArch.logger.w(
            "MviViewModel",
            "sendEvent() should not be called in MviViewModel. Use sendMviEvent() instead. " +
                "Event $event was dropped."
        )
    }

    final override fun showToast(message: String) {
        BrickArch.logger.w(
            "MviViewModel",
            "showToast() should not be called in MviViewModel. Use sendMviEvent() instead. " +
                "Toast message '$message' was dropped."
        )
    }

    final override fun showLoading(show: Boolean) {
        BrickArch.logger.w(
            "MviViewModel",
            "showLoading() should not be called in MviViewModel. Use updateState { copy(isLoading = $show) } instead."
        )
    }

    final override fun navigate(route: String, extras: Map<String, Any>?) {
        BrickArch.logger.w(
            "MviViewModel",
            "navigate() should not be called in MviViewModel. Use sendMviEvent() instead. " +
                "Navigate to '$route' was dropped."
        )
    }

    final override fun navigateBack() {
        BrickArch.logger.w(
            "MviViewModel",
            "navigateBack() should not be called in MviViewModel. Use sendMviEvent() instead."
        )
    }
}
