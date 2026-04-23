package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.LazyLoadHelper
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.nav.AwNav
import kotlinx.coroutines.launch

/**
 * MVVM 架构 Fragment 基类
 *
 * 适用于传统 MVVM 模式的 Fragment，提供了 ViewBinding 支持、ViewModel 自动创建、UI 事件处理和懒加载功能。
 * ViewModel 会根据子类类型自动推断创建，支持通过 [shareViewModelWithActivity] 与 Activity 共享。
 *
 * 与 [MviFragment] 的区别：MVVM 模式更简单，不需要定义 State/Event/Intent，
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
 * @see LazyLoadHelper
 */
abstract class MvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : Fragment(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var viewModel: VM

    private val lazyLoadHelper = LazyLoadHelper(this)

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    open val shareViewModelWithActivity: Boolean = false

    /**
     * 若返回非 null，[UiEvent.Navigate] / [UiEvent.NavigateBack] 将交给 [AwNav] 处理。
     * 典型写法：`override val awNav get() = AwNav.from(this)`（需在宿主 Activity 中已 [AwNav.init]）。
     */
    protected open val awNav: AwNav? get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyLoadHelper.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        lazyLoadHelper.prepareForNewView()
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

    open fun onLazyLoad() {}

    open fun initObservers() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
