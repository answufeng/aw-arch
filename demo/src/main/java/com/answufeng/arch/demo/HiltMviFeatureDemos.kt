package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.answufeng.arch.demo.databinding.ActivityHiltMviDemoBinding
import com.answufeng.arch.hilt.HiltMviActivity
import dagger.hilt.android.AndroidEntryPoint

abstract class BaseHiltMviSingleIntentActivity(
    private val intentToDispatch: HiltCounterIntent
) : HiltMviActivity<
    ActivityHiltMviDemoBinding,
    HiltCounterState,
    HiltCounterEvent,
    HiltCounterIntent,
    HiltCounterViewModel,
>() {
    override val viewModel: HiltCounterViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityHiltMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnIncrement.setOnClickListener { dispatch(HiltCounterIntent.Increment) }
        binding.btnDecrement.setOnClickListener { dispatch(HiltCounterIntent.Decrement) }
        binding.btnReset.setOnClickListener { dispatch(HiltCounterIntent.Reset) }
        binding.btnLoadData.setOnClickListener { dispatch(HiltCounterIntent.LoadData) }

        binding.btnIncrement.isEnabled = intentToDispatch == HiltCounterIntent.Increment
        binding.btnDecrement.isEnabled = intentToDispatch == HiltCounterIntent.Decrement
        binding.btnReset.isEnabled = intentToDispatch == HiltCounterIntent.Reset
        binding.btnLoadData.isEnabled = intentToDispatch == HiltCounterIntent.LoadData
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

@AndroidEntryPoint
class HiltMviIncrementDemoActivity : BaseHiltMviSingleIntentActivity(HiltCounterIntent.Increment)

@AndroidEntryPoint
class HiltMviDecrementDemoActivity : BaseHiltMviSingleIntentActivity(HiltCounterIntent.Decrement)

@AndroidEntryPoint
class HiltMviResetDemoActivity : BaseHiltMviSingleIntentActivity(HiltCounterIntent.Reset)

@AndroidEntryPoint
class HiltMviLoadDataDemoActivity : BaseHiltMviSingleIntentActivity(HiltCounterIntent.LoadData)

