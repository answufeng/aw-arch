package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.demo.databinding.ActivitySimpleMviDemoBinding
import com.answufeng.arch.mvi.SimpleMviActivity
import com.answufeng.arch.mvi.SimpleMviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

data class SimpleDemoState(val count: Int = 0) : UiState

sealed class SimpleDemoIntent : UiIntent {
    data object Inc : SimpleDemoIntent()
    data object Dec : SimpleDemoIntent()
}

class SimpleMviDemoViewModel : SimpleMviViewModel<SimpleDemoState, SimpleDemoIntent>(SimpleDemoState()) {

    override fun handleIntent(intent: SimpleDemoIntent) {
        when (intent) {
            SimpleDemoIntent.Inc -> updateState { copy(count = count + 1) }
            SimpleDemoIntent.Dec -> updateState { copy(count = count - 1) }
        }
    }
}

class SimpleMviDemoActivity :
    SimpleMviActivity<
        ActivitySimpleMviDemoBinding,
        SimpleDemoState,
        SimpleDemoIntent,
        SimpleMviDemoViewModel,
        >() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivitySimpleMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnInc.setOnClickListener { dispatch(SimpleDemoIntent.Inc) }
        binding.btnDec.setOnClickListener { dispatch(SimpleDemoIntent.Dec) }
    }

    override fun render(state: SimpleDemoState) {
        binding.tvCount.text = state.count.toString()
    }
}
