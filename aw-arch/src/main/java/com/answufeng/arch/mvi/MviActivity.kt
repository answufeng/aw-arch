package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

/**
 * MVI 架构 Activity 基类
 *
 * 适用于 MVI 模式的 Activity，提供了 ViewBinding 支持、ViewModel 自动创建和 MVI 状态管理。
 * ViewModel 会根据子类类型自动推断创建。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. [initView] → 3. [initObservers] → 4. [render]（状态变化时）
 *
 * @param VB ViewBinding 类型
 * @param STATE UI 状态类型，必须实现 [UiState]
 * @param EVENT 一次性 Effect 类型，必须实现 [MviEffect]
 * @param INTENT UI 意图类型，必须实现 [UiIntent]
 * @param VM ViewModel 类型，必须继承 [MviViewModel]
 *
 * @see MviViewModel
 * @see UiState
 * @see MviEffect
 * @see UiIntent
 * @see MviDispatcher
 */
abstract class MviActivity<
    VB : ViewBinding,
    STATE : UiState,
    EVENT : MviEffect,
    INTENT : UiIntent,
    VM : MviViewModel<STATE, EVENT, INTENT>,
    > : AppCompatActivity(), MviDispatcher<INTENT> {
    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    private lateinit var archViewModelHolder: VM

    protected open val viewModel: VM
        get() = archViewModelHolder

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        archViewModelHolder = injectViewModel() ?: obtainViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    protected open fun injectViewModel(): VM? = null

    /** 获取 [viewModel]；默认 [createViewModel]，Hilt 子类通过 [injectViewModel] 注入。 */
    protected open fun obtainViewModel(): VM = createViewModel()

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    protected open fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render, handleEvent = ::handleEvent)
    }

    protected open fun viewModelFactory(): ViewModelProvider.Factory? = null

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, MviViewModel::class.java)
        val factory = viewModelFactory()
        val provider = if (factory != null) ViewModelProvider(this, factory) else ViewModelProvider(this)
        @Suppress("UNCHECKED_CAST")
        return provider.get(vmClass) as VM
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
