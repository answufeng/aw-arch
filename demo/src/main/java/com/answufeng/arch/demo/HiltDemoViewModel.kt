package com.answufeng.arch.demo

import com.answufeng.arch.base.MvvmViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HiltDemoViewModel @Inject constructor() : MvvmViewModel() {

    fun showHiltMessage() {
        showToast("ViewModel 由 Hilt 注入")
    }
}
