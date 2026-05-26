package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.answufeng.arch.demo.databinding.ActivitySimpleMviDemoBinding
import com.answufeng.arch.hilt.HiltSimpleMviActivity
import com.answufeng.arch.mvi.SimpleMviViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HiltSimpleMviDemoViewModel @Inject constructor() :
    SimpleMviViewModel<SimpleDemoState, SimpleDemoIntent>(SimpleDemoState()) {
    override fun handleIntent(intent: SimpleDemoIntent) {
        when (intent) {
            SimpleDemoIntent.Inc -> updateState { copy(count = count + 1) }
            SimpleDemoIntent.Dec -> updateState { copy(count = count - 1) }
        }
    }
}

@AndroidEntryPoint
class HiltSimpleMviDemoActivity :
    HiltSimpleMviActivity<
        ActivitySimpleMviDemoBinding,
        SimpleDemoState,
        SimpleDemoIntent,
        HiltSimpleMviDemoViewModel,
        >() {
    override val viewModel: HiltSimpleMviDemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivitySimpleMviDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.title = getString(R.string.demo_hilt_simple_mvi_title)
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnInc.setOnClickListener { dispatch(SimpleDemoIntent.Inc) }
        binding.btnDec.setOnClickListener { dispatch(SimpleDemoIntent.Dec) }
    }

    override fun render(state: SimpleDemoState) {
        binding.tvCount.text = state.count.toString()
    }
}
