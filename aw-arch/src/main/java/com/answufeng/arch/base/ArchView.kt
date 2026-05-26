package com.answufeng.arch.base

import android.os.Bundle

/**
 * MVVM / MVP 共用的视图能力：Loading、Toast、导航等默认实现。
 *
 * - [com.answufeng.arch.mvvm.MvvmView] 在此基础上增加 [com.answufeng.arch.base.MvvmViewModel.UiEvent] 分发
 * - [com.answufeng.arch.mvp.MvpView] 作为 MVP Contract 的标记父接口
 */
interface ArchView {
    fun onLoading(show: Boolean) {}

    fun showToast(message: String) {}

    fun navigateTo(
        route: String,
        extras: Bundle? = null,
    ) {}

    fun navigateBack() {}
}
