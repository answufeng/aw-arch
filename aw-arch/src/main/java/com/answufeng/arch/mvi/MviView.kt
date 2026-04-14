package com.answufeng.arch.mvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

interface MviView<S : UiState, E : UiEvent, I : UiIntent> : LifecycleOwner {

    val viewModel: MviViewModel<S, E, I>

    fun render(state: S)

    fun handleEvent(event: E)

    fun collectStateAndEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { handleEvent(it) }
            }
        }
    }

    fun dispatch(intent: I) = viewModel.dispatch(intent)

    fun dispatchThrottled(intent: I, windowMillis: Long = 300) =
        viewModel.dispatchThrottled(intent, windowMillis)
}
