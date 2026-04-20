package com.answufeng.arch.mvi

/**
 * UI 事件标记接口。
 *
 * 所有 MVI 模式中的一次性事件类都应实现此接口，用于表示不需要持久化的 UI 事件，
 * 如 Toast、Snackbar、导航等。事件消费后不会重放。
 *
 * ```kotlin
 * data class ShowSnackbarEvent(val message: String) : UiEvent
 * ```
 */
interface UiEvent