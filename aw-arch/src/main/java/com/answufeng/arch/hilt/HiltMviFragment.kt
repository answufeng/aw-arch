package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.ext.observeMvi
import com.answufeng.arch.mvi.MviDispatcher
import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

/**
 * Hilt 支持的 MVI 架构 Fragment 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVI 模式 Fragment，提供了 ViewBinding 支持、MVI 状态管理和懒加载功能。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 *
 * @param VB ViewBinding 类型
 * @param STATE UI 状态类型，必须实现 [UiState]
 * @param EVENT UI 事件类型，必须实现 [UiEvent]
 * @param INTENT UI 意图类型，必须实现 [UiIntent]
 * @param VM ViewModel 类型，必须继承 [MviViewModel]
 *
 * @see MviViewModel
 * @see UiState
 * @see UiEvent
 * @see UiIntent
 * @see BaseFragment
 */
abstract class HiltMviFragment<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> :
    BaseFragment<VB>(), MviDispatcher<INTENT> {

    abstract val viewModel: VM

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    override fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render, handleEvent = ::handleEvent)
    }

    override fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    override fun dispatchThrottled(
        intent: INTENT,
        windowMillis: Long,
        keySelector: (INTENT) -> String,
    ) {
        viewModel.dispatchThrottled(intent, windowMillis, keySelector)
    }
}
