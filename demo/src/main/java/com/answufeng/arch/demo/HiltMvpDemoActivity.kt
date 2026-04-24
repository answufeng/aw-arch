package com.answufeng.arch.demo

import com.answufeng.arch.mvp.BaseMvpPresenter
import com.answufeng.arch.mvp.MvpView
import kotlinx.coroutines.delay
import javax.inject.Inject

interface HiltMvpCounterContract {
    interface View : MvpView {
        fun renderCount(count: Int)
    }
}

class HiltMvpCounterPresenter @Inject constructor() : BaseMvpPresenter<HiltMvpCounterContract.View>() {
    private var count: Int = 0

    fun increment() {
        count++
        viewOrNull?.renderCount(count)
    }

    fun decrement() {
        count--
        viewOrNull?.renderCount(count)
    }

    fun reset() {
        count = 0
        viewOrNull?.renderCount(count)
    }

    fun loadData() = launchIO {
        withMain { viewOrNull?.onLoading(true) }
        delay(800)
        count = 77
        withMain {
            viewOrNull?.onLoading(false)
            viewOrNull?.renderCount(count)
            viewOrNull?.showToast("Hilt MVP: loaded = 77")
        }
    }

    fun back() {
        viewOrNull?.navigateBack()
    }
}

// Activity 已拆分为单功能 Feature Activity（见 HiltMvpFeatureDemos.kt）。

