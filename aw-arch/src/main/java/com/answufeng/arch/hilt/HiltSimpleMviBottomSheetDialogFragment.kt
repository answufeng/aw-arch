package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvi.SimpleMviBottomSheetDialogFragment
import com.answufeng.arch.mvi.SimpleMviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

abstract class HiltSimpleMviBottomSheetDialogFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > : SimpleMviBottomSheetDialogFragment<VB, STATE, INTENT, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
