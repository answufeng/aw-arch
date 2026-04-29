package com.answufeng.arch.demo

import com.answufeng.arch.mvp.BaseMvpPresenter
import com.answufeng.arch.mvp.MvpView
import kotlinx.coroutines.delay

interface MvpCounterContract {
    interface View : MvpView {
        fun renderCount(count: Int)
    }
}

class MvpCounterPresenter : BaseMvpPresenter<MvpCounterContract.View>() {
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
        delay(600)
        count = 7
        withMain {
            viewOrNull?.onLoading(false)
            viewOrNull?.renderCount(count)
            viewOrNull?.showToast("MVP: loaded = 7")
        }
    }
}
