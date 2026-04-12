package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.base.BaseViewModel

/**
 * MVVM Fragment 基类。
 *
 * 子类通过 [viewModelClass] 提供 ViewModel 类型，基类自动创建并收集通用 UI 事件。
 * 设置 [shareViewModelWithActivity] = true 可与宿主 Activity 共享 ViewModel。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 */
abstract class MvvmFragment<VB : ViewBinding, VM : BaseViewModel> : BaseFragment<VB>(), MvvmView<VM> {

    protected lateinit var viewModel: VM
        private set

    override val mvvmViewModel: VM get() = viewModel

    override val viewContext: Context get() = requireContext()

    /** 返回 ViewModel 的 Class 对象，子类必须实现 */
    abstract fun viewModelClass(): Class<VM>

    /**
     * 是否与宿主 Activity 共享 ViewModel（默认 false = Fragment 级别）
     */
    open val shareViewModelWithActivity: Boolean = false

    /**
     * 创建 ViewModel 实例。子类可覆写以自定义创建方式（如 Hilt 注入）。
     */
    protected open fun createViewModel(): VM {
        return if (shareViewModelWithActivity) {
            ViewModelProvider(requireActivity())[viewModelClass()]
        } else {
            ViewModelProvider(this)[viewModelClass()]
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
        collectUIEvents()
    }

    override fun onNavigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
