package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvp.MvpBottomSheetDialogFragment
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 版 MVP BottomSheetDialogFragment：继承 [MvpBottomSheetDialogFragment]，子类通过 `override val presenter` 注入。
 */
abstract class HiltMvpBottomSheetDialogFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    MvpBottomSheetDialogFragment<VB, V, P>() {
    abstract override val presenter: P

    override fun injectPresenter(): P = presenter
}
