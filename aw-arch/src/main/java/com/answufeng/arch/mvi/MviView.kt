package com.answufeng.arch.mvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * MVI 视图层辅助接口，提供 State / Event 的生命周期安全收集。
 *
 * 所有 MVI 基类（[MviActivity]、[MviFragment] 等）都实现此接口，
 * 将重复的 collect 逻辑集中到默认方法中，消除代码冗余。
 *
 * ### 实现要求
 * - 实现 [render] 渲染 UI 状态
 * - 实现 [handleEvent] 处理一次性事件
 * - 在合适的生命周期回调中调用 [collectStateAndEvent]
 */
interface MviView<S : UiState, E : UiEvent, I : UiIntent> : LifecycleOwner {

    /** MVI ViewModel 实例 */
    val mviViewModel: MviViewModel<S, E, I>

    /** 渲染 UI 状态（每次 State 变更都会触发） */
    fun render(state: S)

    /** 处理一次性事件（如 Snackbar、导航） */
    fun handleEvent(event: E)

    /**
     * 在 STARTED 状态自动收集 State 和 Event。
     *
     * 应在视图创建完成后调用一次（Activity.onCreate / Fragment.onViewCreated）。
     */
    fun collectStateAndEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mviViewModel.state.collect { render(it) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mviViewModel.event.collect { handleEvent(it) }
            }
        }
    }

    /** 快捷分发 Intent 到 ViewModel */
    fun dispatch(intent: I) = mviViewModel.dispatch(intent)

    /** 带节流的 Intent 分发 */
    fun dispatchThrottled(intent: I, windowMillis: Long = 300) =
        mviViewModel.dispatchThrottled(intent, windowMillis)
}
