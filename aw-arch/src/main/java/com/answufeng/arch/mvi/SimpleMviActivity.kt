package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

abstract class SimpleMviActivity<VB : ViewBinding, STATE : UiState, INTENT : UiIntent> :
    AppCompatActivity(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    protected lateinit var viewModel: SimpleMviViewModel<STATE, INTENT>

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun render(state: STATE)

    protected open fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render)
    }

    protected open fun createViewModel(): SimpleMviViewModel<STATE, INTENT> {
        val vmClass = inferViewModelClass(javaClass, SimpleMviViewModel::class.java)
        return ViewModelProvider(this)[vmClass]
    }

    override fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    override fun dispatchThrottled(intent: INTENT, windowMillis: Long) {
        viewModel.dispatchThrottled(intent, windowMillis)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
