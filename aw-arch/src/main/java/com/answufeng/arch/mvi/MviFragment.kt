package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment

/**
 * MVI Fragment 基类。
 *
 * 子类通过 [viewModelClass] 提供 ViewModel 类型，基类自动创建并收集 State / Event。
 * 设置 [shareViewModelWithActivity] = true 可与宿主 Activity 共享 ViewModel。
 *
 * @see MviActivity
 */
abstract class MviFragment<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : BaseFragment<VB>(), MviView<S, E, I> {

    protected lateinit var viewModel: VM
        private set

    override val mviViewModel: VM get() = viewModel

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
        collectStateAndEvent()
    }
}
