package com.answufeng.arch.hilt

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmView
import com.answufeng.arch.mvvm.dispatchMvvmUiEvent
import com.answufeng.arch.nav.AwNav
import kotlinx.coroutines.launch

/**
 * Hilt 支持的 MVVM 架构 Fragment 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVVM 模式 Fragment，提供了 ViewBinding 支持、UI 事件处理和懒加载功能。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 * @see BaseFragment
 */
abstract class HiltMvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : BaseFragment<VB>(), MvvmView {

    abstract val viewModel: VM

    /** 非 null 时由 [AwNav] 处理导航类 [com.answufeng.arch.base.MvvmViewModel.UiEvent]。 */
    protected open val awNav: AwNav? get() = null

    override fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    dispatchMvvmUiEvent(event, awNav) { navigateBack() }
                }
            }
        }
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
