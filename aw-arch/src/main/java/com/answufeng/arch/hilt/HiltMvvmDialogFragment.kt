package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmDialogFragment

abstract class HiltMvvmDialogFragment<VB : ViewBinding, VM : MvvmViewModel> :
    MvvmDialogFragment<VB, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
