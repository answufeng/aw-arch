package com.answufeng.arch.mvp

import android.os.Bundle

/**
 * MVP 视图基接口。
 *
 * Presenter 通过该接口与 UI 层交互；子类可按需扩展更具体的 Contract.View。
 */
interface MvpView {
    fun onLoading(show: Boolean) {}
    fun showToast(message: String) {}
    fun navigateTo(route: String, extras: Bundle? = null) {}
    fun navigateBack() {}
}

