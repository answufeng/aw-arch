package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.LazyLoadHelper
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

/**
 * MVI 架构 Fragment 基类
 *
 * 适用于 MVI 模式的 Fragment，提供了 ViewBinding 支持、ViewModel 自动创建、MVI 状态管理和懒加载功能。
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
 * @see LazyLoadHelper
 */
abstract class MviFragment<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> :
    Fragment(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var viewModel: VM

    private val lazyLoadHelper = LazyLoadHelper(this)

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    open val shareViewModelWithActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyLoadHelper.onCreate(savedInstanceState)
    }

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

    override fun onResume() {
        super.onResume()
        if (lazyLoadHelper.shouldLazyLoad()) {
            onLazyLoad()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lazyLoadHelper.onSaveInstanceState(outState)
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    open fun onLazyLoad() {}

    protected open fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render, handleEvent = ::handleEvent)
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass(javaClass, MviViewModel::class.java)
        val factory = if (shareViewModelWithActivity) {
            ViewModelProvider(requireActivity())
        } else {
            ViewModelProvider(this)
        }
        return factory[vmClass]
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
