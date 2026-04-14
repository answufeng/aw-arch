package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.base.MvvmViewModel

abstract class MvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : BaseFragment<VB>(), MvvmView<VM> {

    private var _viewModel: VM? = null

    override val viewModel: VM
        get() = _viewModel ?: error("ViewModel not initialized. Ensure onViewCreated is called.")

    override val viewContext: Context get() = requireContext()

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
        collectUIEvents()
    }

    override fun onNavigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
