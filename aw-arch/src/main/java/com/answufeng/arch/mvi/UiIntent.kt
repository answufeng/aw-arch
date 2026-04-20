package com.answufeng.arch.mvi

/**
 * UI 意图标记接口。
 *
 * 所有 MVI 模式中的用户意图类都应实现此接口，用于表示用户的操作意图，
 * 如点击按钮、下拉刷新等。通过 [MviViewModel.dispatch] 分发，由 [MviViewModel.handleIntent] 处理。
 *
 * ```kotlin
 * sealed class CounterIntent : UiIntent {
 *     object Increment : CounterIntent()
 *     object Decrement : CounterIntent()
 * }
 * ```
 */
interface UiIntent