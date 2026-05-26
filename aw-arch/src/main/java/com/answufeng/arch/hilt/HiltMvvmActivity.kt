package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmActivity

/**
 * Hilt 版 MVVM Activity：继承 [MvvmActivity]，子类通过 `override val viewModel: VM by viewModels()` 注入。
 *
 * @param VB ViewBinding 类型
 * @param VM 须继承 [MvvmViewModel]
 */
abstract class HiltMvvmActivity<VB : ViewBinding, VM : MvvmViewModel> :
    MvvmActivity<VB, VM>() {
    abstract override val viewModel: VM

    override fun injectViewModel(): VM = viewModel
}
