package com.answufeng.arch.mvi

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseActivity

abstract class HiltMviActivity<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : MviActivity<VB, S, E, I, VM>() {

    final override fun createViewModel(): VM {
        return ViewModelProvider(this, defaultViewModelProviderFactory)[viewModelClass()]
    }
}
