package com.answufeng.arch.demo

import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

data class HiltCounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
) : UiState

sealed class HiltCounterEvent : UiEvent {
    data class Toast(val message: String) : HiltCounterEvent()
}

sealed class HiltCounterIntent : UiIntent {
    data object Increment : HiltCounterIntent()
    data object Decrement : HiltCounterIntent()
    data object Reset : HiltCounterIntent()
    data object LoadData : HiltCounterIntent()
}

@HiltViewModel
class HiltCounterViewModel @Inject constructor() :
    MviViewModel<HiltCounterState, HiltCounterEvent, HiltCounterIntent>(HiltCounterState()) {

    override fun handleIntent(intent: HiltCounterIntent) {
        when (intent) {
            HiltCounterIntent.Increment -> updateState { copy(count = count + 1) }
            HiltCounterIntent.Decrement -> updateState { copy(count = count - 1) }
            HiltCounterIntent.Reset -> updateState { copy(count = 0) }
            HiltCounterIntent.LoadData -> loadData()
        }
    }

    private fun loadData() = launchIO {
        updateState { copy(isLoading = true) }
        delay(800)
        updateState { copy(isLoading = false, count = 42) }
        sendMviEvent(HiltCounterEvent.Toast("Hilt MVI: loaded = 42"))
    }
}
