package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.ext.observeMvi
import com.answufeng.arch.mvi.MviDispatcher
import com.answufeng.arch.mvi.SimpleMviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

abstract class HiltSimpleMviFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > : BaseFragment<VB>(), MviDispatcher<INTENT> {

    abstract val viewModel: VM

    abstract fun render(state: STATE)

    override fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render)
    }

    override fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    override fun dispatchThrottled(
        intent: INTENT,
        windowMillis: Long,
        keySelector: (INTENT) -> String,
    ) {
        viewModel.dispatchThrottled(intent, windowMillis, keySelector)
    }
}
