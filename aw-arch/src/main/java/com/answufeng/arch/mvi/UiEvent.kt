package com.answufeng.arch.mvi

/**
 * @see MviEffect
 */
@Deprecated(
    message = "Renamed to MviEffect to avoid clashing with MvvmViewModel.UiEvent",
    replaceWith = ReplaceWith("MviEffect", "com.answufeng.arch.mvi.MviEffect"),
)
typealias UiEvent = MviEffect
