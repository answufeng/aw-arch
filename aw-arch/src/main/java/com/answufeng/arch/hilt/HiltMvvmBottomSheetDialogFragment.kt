package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmView
import com.answufeng.arch.mvvm.dispatchMvvmUiEvent
import com.answufeng.arch.nav.AwNav
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

/**
 * Hilt 支持的 MVVM 架构 BottomSheetDialogFragment 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVVM 模式 BottomSheetDialogFragment，提供了 ViewBinding 支持和 UI 事件处理
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 */
abstract class HiltMvvmBottomSheetDialogFragment<VB : ViewBinding, VM : MvvmViewModel> : BottomSheetDialogFragment(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract val viewModel: VM

    protected open val awNav: AwNav? get() = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {
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
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
