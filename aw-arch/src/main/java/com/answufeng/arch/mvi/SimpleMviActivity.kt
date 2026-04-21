package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

/**
 * 简化版 MVI 架构 Activity 基类
 *
 * 与 [MviActivity] 的区别在于不需要定义独立的 Event 类型，适用于不需要单向 UI 事件的简单场景。
 * ViewModel 会自动创建，只需在子类中声明 `override val viewModel` 或让 ViewModel 继承 [SimpleMviViewModel]。
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
abstract class SimpleMviActivity<VB : ViewBinding, STATE : UiState, INTENT : UiIntent> :
    AppCompatActivity(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    protected lateinit var viewModel: SimpleMviViewModel<STATE, INTENT>

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
