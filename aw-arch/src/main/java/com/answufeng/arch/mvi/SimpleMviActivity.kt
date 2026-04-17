package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class SimpleMviActivity<VB : ViewBinding, STATE : UiState, INTENT : UiIntent> : AppCompatActivity() {

    protected lateinit var viewModel: SimpleMviViewModel<STATE, INTENT>
    protected lateinit var binding: VB

    abstract fun viewModelClass(): Class<out SimpleMviViewModel<STATE, INTENT>>
    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[viewModelClass()]
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun render(state: STATE)

    protected open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
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
