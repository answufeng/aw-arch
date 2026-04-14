package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment

abstract class MviFragment<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : BaseFragment<VB>(), MviView<S, E, I> {

    private var _viewModel: VM? = null

    override val viewModel: VM
        get() = _viewModel ?: error("ViewModel not initialized. Ensure onViewCreated is called.")

    abstract fun viewModelClass(): Class<VM>

    open val shareViewModelWithActivity: Boolean = false

    protected open fun createViewModel(): VM {
        return if (shareViewModelWithActivity) {
            ViewModelProvider(requireActivity())[viewModelClass()]
        } else {
            ViewModelProvider(this)[viewModelClass()]
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
        collectStateAndEvent()
    }
}
