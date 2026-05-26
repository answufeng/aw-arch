package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvi.MviActivity
import com.answufeng.arch.mvi.MviEffect
import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

/**
 * Hilt 版 MVI Activity：子类 `override val viewModel: VM by viewModels()`。
 */
abstract class HiltMviActivity<
    VB : ViewBinding,
    STATE : UiState,
    EVENT : MviEffect,
    INTENT : UiIntent,
    VM : MviViewModel<STATE, EVENT, INTENT>,
    > : MviActivity<VB, STATE, EVENT, INTENT, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
