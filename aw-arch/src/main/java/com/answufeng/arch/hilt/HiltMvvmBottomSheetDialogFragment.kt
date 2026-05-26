package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmBottomSheetDialogFragment

abstract class HiltMvvmBottomSheetDialogFragment<VB : ViewBinding, VM : MvvmViewModel> :
    MvvmBottomSheetDialogFragment<VB, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
