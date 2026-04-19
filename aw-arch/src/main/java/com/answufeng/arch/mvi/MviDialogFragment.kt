package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferViewModelClass
import com.answufeng.arch.ext.observeMvi

abstract class MviDialogFragment<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> :
    DialogFragment(), MviDispatcher<INTENT> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var viewModel: VM

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun render(state: STATE)

    open fun handleEvent(event: EVENT) {}

    protected open fun initObservers() {
        observeMvi(viewModel.state, viewModel.event, render = ::render, handleEvent = ::handleEvent)
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass(javaClass, MviViewModel::class.java)
        return ViewModelProvider(this)[vmClass]
    }

    override fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }

    override fun dispatchThrottled(intent: INTENT, windowMillis: Long) {
        viewModel.dispatchThrottled(intent, windowMillis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
