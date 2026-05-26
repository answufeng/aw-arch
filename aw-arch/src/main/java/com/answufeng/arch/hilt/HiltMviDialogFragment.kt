package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvi.MviDialogFragment
import com.answufeng.arch.mvi.MviEffect
import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

abstract class HiltMviDialogFragment<
    VB : ViewBinding,
    STATE : UiState,
    EVENT : MviEffect,
    INTENT : UiIntent,
    VM : MviViewModel<STATE, EVENT, INTENT>,
    > : MviDialogFragment<VB, STATE, EVENT, INTENT, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
