package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 * 简化版 MVI ViewModel，不使用自定义 Event 类型。
 *
 * 适用于不需要一次性事件（如 Snackbar、导航）的简单页面，
 * 所有 UI 变化通过 State 驱动。
 *
 * ```kotlin
 * class CounterViewModel : SimpleMviViewModel<CounterState, CounterIntent>(CounterState()) {
 *     override fun handleIntent(intent: CounterIntent) {
 *         when (intent) {
 *             CounterIntent.Increment -> updateState { copy(count = count + 1) }
 *             CounterIntent.Decrement -> updateState { copy(count = count - 1) }
 *         }
 *     }
 * }
 * ```
 *
 * @param S 页面状态
 * @param I 用户意图
 */
abstract class SimpleMviViewModel<S : UiState, I : UiIntent>(
    initialState: S
) : MviViewModel<S, UiEvent, I>(initialState)

/**
 * MVI Activity 简化基类，搭配 [SimpleMviViewModel] 使用。
 *
 * 将 5 个泛型参数减少到 3 个（省略 Event 和 ViewModel），
 * 适用于不需要自定义一次性事件的简单页面。
 *
 * ```kotlin
 * class CounterActivity : SimpleMviActivity<
 *     ActivityCounterBinding, CounterState, CounterIntent
 * >() {
 *     override fun viewModelClass() = CounterViewModel::class.java
 *     override fun inflateBinding(inflater: LayoutInflater) =
 *         ActivityCounterBinding.inflate(inflater)
 *     override fun initView(savedInstanceState: Bundle?) { ... }
 *     override fun render(state: CounterState) { ... }
 *     override fun handleEvent(event: UiEvent) { /* 默认空实现 */ }
 * }
 * ```
 *
 * @param VB ViewBinding
 * @param S  页面状态
 * @param I  用户意图
 */
abstract class SimpleMviActivity<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviActivity<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}

/**
 * MVI Fragment 简化基类，搭配 [SimpleMviViewModel] 使用。
 *
 * @param VB ViewBinding
 * @param S  页面状态
 * @param I  用户意图
 */
abstract class SimpleMviFragment<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviFragment<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}
