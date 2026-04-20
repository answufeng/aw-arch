package com.answufeng.arch.mvi

import androidx.lifecycle.SavedStateHandle

/**
 * 简化版 MVI ViewModel，省略了事件层（[UiEvent]）。
 *
 * 适用于不需要一次性事件的场景，仅保留状态（[UiState]）和意图（[UiIntent]）两层抽象。
 * 内部使用 [NoEvent] 作为事件类型的占位。
 *
 * ```kotlin
 * class CounterViewModel(initialState: CounterState) :
 *     SimpleMviViewModel<CounterState, CounterIntent>(initialState) {
 *     override fun handleIntent(intent: CounterIntent) {
 *         updateState { copy(count = count + 1) }
 *     }
 * }
 * ```
 *
 * @param S 状态类型，必须实现 [UiState]
 * @param I 意图类型，必须实现 [UiIntent]
 * @param initialState 初始状态
 * @param savedStateHandle 进程重启后恢复状态
 */
abstract class SimpleMviViewModel<S : UiState, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : MviViewModel<S, NoEvent, I>(initialState, savedStateHandle)

/**
 * [SimpleMviViewModel] 的空事件占位对象，表示无需事件。
 */
object NoEvent : UiEvent
