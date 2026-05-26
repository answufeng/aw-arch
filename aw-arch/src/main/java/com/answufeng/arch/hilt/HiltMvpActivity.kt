package com.answufeng.arch.hilt

import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvp.MvpActivity
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 版 MVP Activity：继承 [MvpActivity]，子类通过 `override val presenter` 注入。
 */
abstract class HiltMvpActivity<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    MvpActivity<VB, V, P>() {
    abstract override val presenter: P

    override fun injectPresenter(): P = presenter
}
