package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.answufeng.arch.demo.databinding.ActivityHiltMviDemoBinding
import com.answufeng.arch.hilt.HiltMviActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltMviDemoActivity : HiltMviActivity<
    ActivityHiltMviDemoBinding,
    HiltCounterState,
    HiltCounterEvent,
    HiltCounterIntent,
    HiltCounterViewModel,
>() {
    override val viewModel: HiltCounterViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityHiltMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnIncrement.setOnClickListener { dispatch(HiltCounterIntent.Increment) }
        binding.btnDecrement.setOnClickListener { dispatch(HiltCounterIntent.Decrement) }
        binding.btnReset.setOnClickListener { dispatch(HiltCounterIntent.Reset) }
        binding.btnLoadData.setOnClickListener { dispatch(HiltCounterIntent.LoadData) }
    }

    override fun render(state: HiltCounterState) {
        binding.tvCount.text = state.count.toString()
        binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun handleEvent(event: HiltCounterEvent) {
        when (event) {
            is HiltCounterEvent.Toast ->
                android.widget.Toast.makeText(this, event.message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
