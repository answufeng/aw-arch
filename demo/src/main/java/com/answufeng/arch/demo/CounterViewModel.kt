package com.answufeng.arch.demo

import com.answufeng.arch.mvi.MviEffect
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState
import com.answufeng.arch.mvi.MviViewModel
import kotlinx.coroutines.delay

data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false
) : UiState

sealed class CounterEffect : MviEffect {
    data class ShowSnackbar(val message: String) : CounterEffect()
}

sealed class CounterIntent : UiIntent {
    data object Increment : CounterIntent()
    data object Decrement : CounterIntent()
    data object Reset : CounterIntent()
    data object LoadData : CounterIntent()
}

class CounterViewModel : MviViewModel<CounterState, CounterEffect, CounterIntent>(CounterState()) {

    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> updateState { copy(count = count + 1) }
            CounterIntent.Decrement -> updateState { copy(count = count - 1) }
            CounterIntent.Reset -> updateState { copy(count = 0) }
            CounterIntent.LoadData -> loadData()
        }
    }

    private fun loadData() = launchIO {
        updateState { copy(isLoading = true) }
        delay(1500)
        updateState { copy(isLoading = false, count = 100) }
        sendMviEvent(CounterEffect.ShowSnackbar("Data loaded!"))
    }
}
