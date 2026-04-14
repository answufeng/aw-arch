package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseActivity
import com.answufeng.arch.base.MvvmViewModel

abstract class HiltMvvmActivity<VB : ViewBinding, VM : MvvmViewModel>
    : MvvmActivity<VB, VM>() {

    final override fun createViewModel(): VM {
        return ViewModelProvider(this, defaultViewModelProviderFactory)[viewModelClass()]
    }
}
