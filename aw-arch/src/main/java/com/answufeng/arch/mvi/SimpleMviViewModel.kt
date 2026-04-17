package com.answufeng.arch.mvi

import androidx.lifecycle.SavedStateHandle

abstract class SimpleMviViewModel<S : UiState, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : MviViewModel<S, NoEvent, I>(initialState, savedStateHandle)

object NoEvent : UiEvent
