package com.answufeng.arch.mvp

/**
 * MVP Presenter 基接口。
 *
 * - [attachView]/[detachView] 由基类 Activity/Fragment 等在生命周期中自动调用。
 * - 子类可覆写 [onViewAttached]/[onViewDetached] 来订阅/取消订阅。
 */
interface MvpPresenter<V : MvpView> {
    fun attachView(view: V)
    fun detachView()

    /** 当前是否已绑定 View。*/
    val isViewAttached: Boolean
}

