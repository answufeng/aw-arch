package com.answufeng.arch.mvi

/**
 * UI 状态标记接口。
 *
 * 所有 MVI 模式中的状态类都应实现此接口，用于表示屏幕的完整 UI 状态。
 * 状态是不可变的快照，UI 层通过订阅 [StateFlow][kotlinx.coroutines.flow.StateFlow] 自动渲染。
 *
 * ```kotlin
 * data class CounterState(val count: Int = 0, val isLoading: Boolean = false) : UiState
 * ```
 */
interface UiState