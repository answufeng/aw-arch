package com.answufeng.arch.demo

import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState
import com.answufeng.arch.mvi.MviViewModel
import kotlinx.coroutines.delay

data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false
) : UiState

sealed class CounterEvent : UiEvent {
    data class ShowSnackbar(val message: String) : CounterEvent()
}

sealed class CounterIntent : UiIntent {
    data object Increment : CounterIntent()
    data object Decrement : CounterIntent()
    data object Reset : CounterIntent()
    data object LoadData : CounterIntent()
}

class CounterViewModel : MviViewModel<CounterState, CounterEvent, CounterIntent>(CounterState()) {

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
        sendMviEvent(CounterEvent.ShowSnackbar("Data loaded!"))
    }
}
// Activity 已拆分为单 Intent 的 Feature Activity（见 MviFeatureDemos.kt）。
