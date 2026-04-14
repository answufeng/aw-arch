package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseBottomSheetDialogFragment
import com.answufeng.arch.base.MvvmViewModel

abstract class HiltMvvmBottomSheetDialogFragment<VB : ViewBinding, VM : MvvmViewModel>
    : MvvmBottomSheetDialogFragment<VB, VM>() {

    final override fun createViewModel(): VM {
        return ViewModelProvider(this, defaultViewModelProviderFactory)[viewModelClass()]
    }
}
