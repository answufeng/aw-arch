package com.answufeng.arch.mvi

/**
 * MVI 一次性 UI 副作用标记接口（如 Toast、导航、Snackbar）。
 *
 * 与 [com.answufeng.arch.base.MvvmViewModel.UiEvent] 区分命名，避免 import 混淆。
 * 事件消费后不会重放。
 *
 * ```kotlin
 * data class ShowSnackbarEffect(val message: String) : MviEffect
 * ```
 */
interface MviEffect
