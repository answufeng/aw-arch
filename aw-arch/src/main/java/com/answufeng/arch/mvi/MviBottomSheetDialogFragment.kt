package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.observeMvi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class MviBottomSheetDialogFragment<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> :
    BottomSheetDialogFragment(), MviDispatcher<INTENT> {

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

    @Suppress("UNCHECKED_CAST")
    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass()
        return ViewModelProvider(this)[vmClass]
    }

    @Suppress("UNCHECKED_CAST")
    private fun inferViewModelClass(): Class<VM> {
        val superclass = javaClass.genericSuperclass
        if (superclass is java.lang.reflect.ParameterizedType) {
            val types = superclass.actualTypeArguments
            for (type in types) {
                if (type is Class<*> && MviViewModel::class.java.isAssignableFrom(type)) {
                    return type as Class<VM>
                }
            }
        }
        throw IllegalStateException("Cannot infer ViewModel class. Override createViewModel() or specify generic type parameters.")
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
