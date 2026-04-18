package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.observeMvi

abstract class SimpleMviActivity<VB : ViewBinding, STATE : UiState, INTENT : UiIntent> :
    AppCompatActivity(), MviDispatcher<INTENT> {

    protected lateinit var viewModel: SimpleMviViewModel<STATE, INTENT>
    protected lateinit var binding: VB

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
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

    @Suppress("UNCHECKED_CAST")
    protected open fun createViewModel(): SimpleMviViewModel<STATE, INTENT> {
        val vmClass = inferViewModelClass()
        return ViewModelProvider(this)[vmClass]
    }

    @Suppress("UNCHECKED_CAST")
    private fun inferViewModelClass(): Class<out SimpleMviViewModel<STATE, INTENT>> {
        val superclass = javaClass.genericSuperclass
        if (superclass is java.lang.reflect.ParameterizedType) {
            val types = superclass.actualTypeArguments
            for (type in types) {
                if (type is Class<*> && SimpleMviViewModel::class.java.isAssignableFrom(type)) {
                    return type as Class<out SimpleMviViewModel<STATE, INTENT>>
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
}
