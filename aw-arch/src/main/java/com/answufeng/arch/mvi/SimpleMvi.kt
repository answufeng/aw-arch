package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class SimpleMviViewModel<S : UiState, I : UiIntent>(
    initialState: S
) : MviViewModel<S, UiEvent, I>(initialState)

abstract class SimpleMviActivity<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviActivity<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}

abstract class SimpleMviFragment<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviFragment<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}

abstract class SimpleMviDialogFragment<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviDialogFragment<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}

abstract class SimpleMviBottomSheetDialogFragment<VB : ViewBinding, S : UiState, I : UiIntent>
    : MviBottomSheetDialogFragment<VB, S, UiEvent, I, SimpleMviViewModel<S, I>>() {

    override fun handleEvent(event: UiEvent) {}
}
