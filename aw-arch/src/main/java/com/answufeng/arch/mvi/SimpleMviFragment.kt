package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

/**
 * 简化版 MVI 架构 Fragment 基类
 *
 * 与 [MviFragment] 的区别在于不需要定义独立的 Event 类型，适用于不需要单向 UI 事件的简单场景。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 * 支持懒加载，首次对用户可见时才调用 [onLazyLoad]。
 * 子类须声明第四类型参数 [VM]（具体 [SimpleMviViewModel] 实现），以便反射创建。
 *
 * @param VB ViewBinding 类型
 * @param STATE UI 状态类型，必须实现 [UiState]
 * @param INTENT UI 意图类型，必须实现 [UiIntent]
 * @param VM 必须继承 [SimpleMviViewModel] 且泛型为 [STATE]、[INTENT]
 *
 * @see SimpleMviViewModel
 * @see UiState
 * @see UiIntent
 * @see MviDispatcher
 * @see BaseFragment
 */
abstract class SimpleMviFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > :
    BaseFragment<VB>(), MviDispatcher<INTENT> {

    protected lateinit var viewModel: VM

    open val shareViewModelWithActivity: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    abstract fun render(state: STATE)

    override fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render)
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, SimpleMviViewModel::class.java)
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
