package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseBottomSheetDialogFragment
import com.answufeng.arch.base.MvvmViewModel

abstract class MvvmBottomSheetDialogFragment<VB : ViewBinding, VM : MvvmViewModel>
    : BaseBottomSheetDialogFragment<VB>(), MvvmView<VM> {

    private var _viewModel: VM? = null

    override val viewModel: VM
        get() = _viewModel ?: error("ViewModel not initialized. Ensure onViewCreated is called.")

    override val viewContext: Context get() = requireContext()

    abstract fun viewModelClass(): Class<VM>

    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _viewModel = createViewModel()
        super.onViewCreated(view, savedInstanceState)
        collectUIEvents()
    }

    override fun onNavigateBack() {
        dismiss()
    }
}
