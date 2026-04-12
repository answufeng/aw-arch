package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseDialogFragment

/**
 * MVI DialogFragment 基类。
 *
 * 自动创建 ViewModel 并收集 State / Event，子类实现 [render] 和 [handleEvent]。
 *
 * ```kotlin
 * class EditDialog : MviDialogFragment<
 *     DialogEditBinding, EditState, EditEvent, EditIntent, EditViewModel
 * >() {
 *     override fun viewModelClass() = EditViewModel::class.java
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         DialogEditBinding.inflate(inflater, container, false)
 *     override fun initView() {
 *         binding.btnSave.setOnClickListener { dispatch(EditIntent.Save) }
 *     }
 *     override fun render(state: EditState) { /* ... */ }
 *     override fun handleEvent(event: EditEvent) { /* ... */ }
 * }
 * ```
 */
abstract class MviDialogFragment<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : BaseDialogFragment<VB>(), MviView<S, E, I> {

    protected lateinit var viewModel: VM
        private set

    override val mviViewModel: VM get() = viewModel

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
        collectStateAndEvent()
    }
}
