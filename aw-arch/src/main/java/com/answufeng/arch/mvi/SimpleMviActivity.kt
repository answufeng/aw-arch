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
 * 子类须声明 `SimpleMviActivity<VB, STATE, INTENT, VM>`，其中 [VM] 为具体的 [SimpleMviViewModel] 实现类型，供反射创建。
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
 */
abstract class SimpleMviActivity<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > :
    AppCompatActivity(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    protected lateinit var viewModel: VM

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

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, SimpleMviViewModel::class.java)
        @Suppress("UNCHECKED_CAST")
        return ViewModelProvider(this).get(vmClass) as VM
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
