package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiState
import kotlinx.coroutines.launch

abstract class HiltMviActivity<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : com.answufeng.arch.mvi.UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> : AppCompatActivity() {

    protected lateinit var binding: VB

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    abstract val viewModel: VM

    protected open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(it) } }
                launch { viewModel.event.collect { handleEvent(it) } }
            }
        }
    }

    protected fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    protected fun dispatchThrottled(intent: INTENT, windowMillis: Long = 300) {
        viewModel.dispatchThrottled(intent, windowMillis)
    }
}
