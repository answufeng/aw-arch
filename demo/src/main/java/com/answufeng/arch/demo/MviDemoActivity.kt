package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.mvi.MviActivity
import com.answufeng.arch.demo.databinding.ActivityMviDemoBinding

class MviDemoActivity : MviActivity<ActivityMviDemoBinding, CounterState, CounterEvent, CounterIntent, CounterViewModel>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

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
