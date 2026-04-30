package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

/**
 * MVI 架构 Fragment 基类
 *
 * 适用于 MVI 模式的 Fragment，提供了 ViewBinding 支持、ViewModel 自动创建、MVI 状态管理和懒加载功能。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 * ViewModel 会根据子类类型自动推断创建，支持通过 [shareViewModelWithActivity] 与 Activity 共享。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. [initView] → 3. [initObservers] → 4. [render]（状态变化时）→ 5. [onLazyLoad]（首次可见时）
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
 * @see MviDispatcher
 * @see BaseFragment
 */
abstract class MviFragment<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> :
    BaseFragment<VB>(), MviDispatcher<INTENT> {

    protected lateinit var viewModel: VM

    open val shareViewModelWithActivity: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    override fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render, handleEvent = ::handleEvent)
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, MviViewModel::class.java)
        val factory = if (shareViewModelWithActivity) {
            ViewModelProvider(requireActivity())
        } else {
            ViewModelProvider(this)
        }
        @Suppress("UNCHECKED_CAST")
        return factory.get(vmClass) as VM
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
