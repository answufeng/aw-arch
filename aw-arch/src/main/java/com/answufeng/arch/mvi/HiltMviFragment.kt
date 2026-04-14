package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment

abstract class HiltMviFragment<VB : ViewBinding, S : UiState, E : UiEvent, I : UiIntent, VM : MviViewModel<S, E, I>>
    : MviFragment<VB, S, E, I, VM>() {

    final override fun createViewModel(): VM {
        return ViewModelProvider(this, defaultViewModelProviderFactory)[viewModelClass()]
    }
}
