package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UiEvent
import com.answufeng.arch.ext.inferViewModelClass
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

/**
 * MVVM 架构 BottomSheetDialogFragment 基类
 *
 * 适用于传统 MVVM 模式的底部弹窗，提供了 ViewBinding 支持和 UI 事件处理。
 * ViewModel 会根据子类类型自动推断创建。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 */
abstract class MvvmBottomSheetDialogFragment<VB : ViewBinding, VM : MvvmViewModel> : BottomSheetDialogFragment(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var viewModel: VM

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

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.UiEvent.collect { onUiEvent(it) }
            }
        }
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass(javaClass, MvvmViewModel::class.java)
        return ViewModelProvider(this)[vmClass]
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
