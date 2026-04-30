package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.nav.AwNav
import kotlinx.coroutines.launch

/**
 * MVVM 架构 Fragment 基类
 *
 * 适用于传统 MVVM 模式的 Fragment，提供了 ViewBinding 支持、ViewModel 自动创建、UI 事件处理和懒加载功能。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 * ViewModel 会根据子类类型自动推断创建，支持通过 [shareViewModelWithActivity] 与 Activity 共享。
 *
 * 与 [com.answufeng.arch.mvi.MviFragment] 的区别：MVVM 模式更简单，不需要定义 State/Event/Intent，
 * 适合不需要严格单向数据流的场景。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. [initView] → 3. [initObservers] → 4. [onLazyLoad]（首次可见时）
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 * @see BaseFragment
 */
abstract class MvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : BaseFragment<VB>(), MvvmView {

    protected lateinit var viewModel: VM

    open val shareViewModelWithActivity: Boolean = false

    /**
     * 若返回非 null，[com.answufeng.arch.base.MvvmViewModel.UiEvent.Navigate] / [com.answufeng.arch.base.MvvmViewModel.UiEvent.NavigateBack] 将交给 [AwNav] 处理。
     * 典型写法：`override val awNav get() = AwNav.from(this)`（需在宿主 Activity 中已 [AwNav.init]）。
     */
    protected open val awNav: AwNav? get() = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    dispatchMvvmUiEvent(event, awNav) { navigateBack() }
                }
            }
        }
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, MvvmViewModel::class.java)
        val factory = if (shareViewModelWithActivity) {
            ViewModelProvider(requireActivity())
        } else {
            ViewModelProvider(this)
        }
        @Suppress("UNCHECKED_CAST")
        return factory.get(vmClass) as VM
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
