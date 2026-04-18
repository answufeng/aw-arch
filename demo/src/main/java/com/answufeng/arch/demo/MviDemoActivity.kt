package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.demo.databinding.ActivityMviDemoBinding
import com.answufeng.arch.mvi.MviActivity
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

class MviDemoActivity : MviActivity<
    ActivityMviDemoBinding, CounterState, CounterEvent, CounterIntent, CounterViewModel
>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnIncrement.setOnClickListener { dispatch(CounterIntent.Increment) }
        binding.btnDecrement.setOnClickListener { dispatch(CounterIntent.Decrement) }
        binding.btnReset.setOnClickListener { dispatch(CounterIntent.Reset) }
        binding.btnLoadData.setOnClickListener { dispatch(CounterIntent.LoadData) }
    }

    override fun render(state: CounterState) {
        binding.tvCount.text = state.count.toString()
        binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun handleEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.ShowSnackbar ->
                android.widget.Toast.makeText(this, event.message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
