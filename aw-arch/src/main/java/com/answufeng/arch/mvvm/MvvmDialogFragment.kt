package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseDialogFragment
import com.answufeng.arch.base.BaseViewModel

/**
 * MVVM DialogFragment 基类。
 *
 * 自动创建 ViewModel 并收集通用 UI 事件。
 *
 * ```kotlin
 * class ConfirmDialog : MvvmDialogFragment<DialogConfirmBinding, ConfirmViewModel>() {
 *     override fun viewModelClass() = ConfirmViewModel::class.java
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         DialogConfirmBinding.inflate(inflater, container, false)
 *     override fun initView() {
 *         binding.btnOk.setOnClickListener { viewModel.confirm() }
 *     }
 * }
 * ```
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 */
abstract class MvvmDialogFragment<VB : ViewBinding, VM : BaseViewModel> : BaseDialogFragment<VB>(), MvvmView<VM> {

    protected lateinit var viewModel: VM
        private set

    override val mvvmViewModel: VM get() = viewModel

    override val viewContext: Context get() = requireContext()

    /** 返回 ViewModel 的 Class 对象 */
    abstract fun viewModelClass(): Class<VM>

    /**
     * 创建 ViewModel 实例。子类可覆写以自定义创建方式（如 Hilt 注入）。
     */
    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
        collectUIEvents()
    }

    override fun onNavigateBack() {
        dismiss()
    }
}
