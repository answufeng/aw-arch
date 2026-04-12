package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseBottomSheetDialogFragment

/**
 * MVI BottomSheetDialogFragment 基类。
 *
 * 自动创建 ViewModel 并收集 State / Event，继承 [BaseBottomSheetDialogFragment] 的所有配置能力。
 *
 * ```kotlin
 * class FilterBottomSheet : MviBottomSheetDialogFragment<
 *     DialogFilterBinding, FilterState, FilterEvent, FilterIntent, FilterViewModel
 * >() {
 *     override fun viewModelClass() = FilterViewModel::class.java
 *     override val peekHeight = 400
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         DialogFilterBinding.inflate(inflater, container, false)
 *     override fun initView() { /* ... */ }
 *     override fun render(state: FilterState) { /* ... */ }
 *     override fun handleEvent(event: FilterEvent) { /* ... */ }
 * }
 * ```
 */
abstract class MviBottomSheetDialogFragment<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : BaseBottomSheetDialogFragment<VB>(), MviView<S, E, I> {

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
