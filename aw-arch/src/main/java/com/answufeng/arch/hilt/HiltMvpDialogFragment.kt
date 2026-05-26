package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvp.MvpDialogFragment
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 版 MVP DialogFragment：继承 [MvpDialogFragment]，子类通过 `override val presenter` 注入。
 */
abstract class HiltMvpDialogFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    MvpDialogFragment<VB, V, P>() {
    abstract override val presenter: P

    override fun injectPresenter(): P = presenter
}
