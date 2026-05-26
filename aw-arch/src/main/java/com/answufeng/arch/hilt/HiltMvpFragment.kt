package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvp.MvpFragment
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 版 MVP Fragment：继承 [MvpFragment]，子类通过 `override val presenter` 注入。
 */
abstract class HiltMvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    MvpFragment<VB, V, P>() {
    abstract override val presenter: P

    override fun injectPresenter(): P = presenter
}
