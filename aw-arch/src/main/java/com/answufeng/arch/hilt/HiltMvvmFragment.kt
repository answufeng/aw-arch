package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmFragment

/**
 * Hilt 版 MVVM Fragment：继承 [MvvmFragment]，子类通过 `override val viewModel: VM by viewModels()` 注入。
 */
abstract class HiltMvvmFragment<VB : ViewBinding, VM : MvvmViewModel> :
    MvvmFragment<VB, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
