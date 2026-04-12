package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseActivity

/**
 * MVI Activity 基类。
 *
 * 子类通过 [viewModelClass] 提供 ViewModel 类型，基类自动创建并在 STARTED 状态收集
 * [MviViewModel.state] 和 [MviViewModel.event]，子类只需实现 [render] 和 [handleEvent]。
 *
 * ```kotlin
 * class CounterActivity : MviActivity<
 *     ActivityCounterBinding, CounterState, CounterEvent, CounterIntent, CounterViewModel
 * >() {
 *     override fun viewModelClass() = CounterViewModel::class.java
 *
 *     override fun inflateBinding(inflater: LayoutInflater) =
 *         ActivityCounterBinding.inflate(inflater)
 *
 *     override fun initView(savedInstanceState: Bundle?) {
 *         binding.btnAdd.setOnClickListener { dispatch(CounterIntent.Increment) }
 *     }
 *
 *     override fun render(state: CounterState) {
 *         binding.tvCount.text = state.count.toString()
 *     }
 *
 *     override fun handleEvent(event: CounterEvent) { /* one-shot events */ }
 * }
 * ```
 *
 * @param VB ViewBinding
 * @param S  页面状态
 * @param E  一次性事件
 * @param I  用户意图
 * @param VM MviViewModel
 */
abstract class MviActivity<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : BaseActivity<VB>(), MviView<S, E, I> {

    protected lateinit var viewModel: VM
        private set

    override val mviViewModel: VM get() = viewModel

    /** 返回 ViewModel 的 Class 对象，子类必须实现 */
    abstract fun viewModelClass(): Class<VM>

    /**
     * 创建 ViewModel 实例。子类可覆写以自定义创建方式（如 Hilt 注入）。
     */
    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    override fun onPreInit(savedInstanceState: Bundle?) {
        viewModel = createViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectStateAndEvent()
    }
}
