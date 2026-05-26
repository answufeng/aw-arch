package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvi.SimpleMviDialogFragment
import com.answufeng.arch.mvi.SimpleMviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

abstract class HiltSimpleMviDialogFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > : SimpleMviDialogFragment<VB, STATE, INTENT, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
