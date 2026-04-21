package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 简化版 MVI 架构 BottomSheetDialogFragment 基类
 *
 * 与 [SimpleMviFragment] 类似，但继承 BottomSheetDialogFragment，适用于底部弹窗场景。
 * 不需要定义独立的 Event 类型，适用于不需要单向 UI 事件的简单场景。
 * ViewModel 会根据子类类型自动推断创建。
 *
 * @param VB ViewBinding 类型
 * @param STATE UI 状态类型，必须实现 [UiState]
 * @param INTENT UI 意图类型，必须实现 [UiIntent]
 *
 * @see SimpleMviViewModel
 * @see UiState
 * @see UiIntent
 * @see MviDispatcher
 */
abstract class SimpleMviBottomSheetDialogFragment<VB : ViewBinding, STATE : UiState, INTENT : UiIntent> :
    BottomSheetDialogFragment(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var viewModel: SimpleMviViewModel<STATE, INTENT>

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun render(state: STATE)

    protected open fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render)
    }

    protected open fun createViewModel(): SimpleMviViewModel<STATE, INTENT> {
        val vmClass = inferViewModelClass(javaClass, SimpleMviViewModel::class.java)
        return ViewModelProvider(this)[vmClass]
    }

    override fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    override fun dispatchThrottled(intent: INTENT, windowMillis: Long) {
        viewModel.dispatchThrottled(intent, windowMillis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
