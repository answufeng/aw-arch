package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseDialogFragment
import com.answufeng.arch.base.MvvmViewModel

abstract class HiltMvvmDialogFragment<VB : ViewBinding, VM : MvvmViewModel>
    : MvvmDialogFragment<VB, VM>() {

    final override fun createViewModel(): VM {
        return ViewModelProvider(this, defaultViewModelProviderFactory)[viewModelClass()]
    }
}
