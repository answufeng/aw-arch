package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.demo.databinding.ActivityMviDemoBinding
import com.answufeng.arch.mvi.MviActivity

abstract class BaseMviSingleIntentActivity(
    private val intentToDispatch: CounterIntent
) : MviActivity<ActivityMviDemoBinding, CounterState, CounterEvent, CounterIntent, CounterViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnIncrement.setOnClickListener { dispatch(CounterIntent.Increment) }
        binding.btnDecrement.setOnClickListener { dispatch(CounterIntent.Decrement) }
        binding.btnReset.setOnClickListener { dispatch(CounterIntent.Reset) }
        binding.btnLoadData.setOnClickListener { dispatch(CounterIntent.LoadData) }

        // 只保留一个功能按钮，其余置灰
        binding.btnIncrement.isEnabled = intentToDispatch == CounterIntent.Increment
        binding.btnDecrement.isEnabled = intentToDispatch == CounterIntent.Decrement
        binding.btnReset.isEnabled = intentToDispatch == CounterIntent.Reset
        binding.btnLoadData.isEnabled = intentToDispatch == CounterIntent.LoadData
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

class MviIncrementDemoActivity : BaseMviSingleIntentActivity(CounterIntent.Increment)
class MviDecrementDemoActivity : BaseMviSingleIntentActivity(CounterIntent.Decrement)
class MviResetDemoActivity : BaseMviSingleIntentActivity(CounterIntent.Reset)
class MviLoadDataDemoActivity : BaseMviSingleIntentActivity(CounterIntent.LoadData)

