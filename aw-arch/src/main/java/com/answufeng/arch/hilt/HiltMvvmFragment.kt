package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.LazyLoadHelper
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UiEvent
import com.answufeng.arch.mvvm.MvvmView
import kotlinx.coroutines.launch

/**
 * Hilt 支持的 MVVM 架构 Fragment 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVVM 模式 Fragment，提供了 ViewBinding 支持、UI 事件处理和懒加载功能
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 * @see LazyLoadHelper
 */
abstract class HiltMvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : Fragment(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract val viewModel: VM

    private val lazyLoadHelper = LazyLoadHelper(this)

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

    open fun onLazyLoad() {}

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.UiEvent.collect { onUiEvent(it) }
            }
        }
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
