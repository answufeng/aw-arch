package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.demo.databinding.ActivityMvvmDemoBinding
import com.answufeng.arch.mvvm.MvvmActivity
import kotlinx.coroutines.delay

class MvvmDemoActivity : MvvmActivity<ActivityMvvmDemoBinding, MvvmDemoViewModel>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityMvvmDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnLoadData.setOnClickListener { viewModel.loadData() }
        binding.btnShowToast.setOnClickListener { viewModel.triggerToast() }
        binding.btnNavigateBack.setOnClickListener { viewModel.goBack() }
        binding.btnError.setOnClickListener { viewModel.triggerError() }
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}

class MvvmDemoViewModel : MvvmViewModel() {

    fun loadData() = launchIO {
        showLoading(true)
        delay(1500)
        showLoading(false)
        showToast("Data loaded!")
    }

    fun triggerToast() {
        showToast("Hello from MVVM!")
    }

    fun triggerError() = launch {
        throw RuntimeException("This is a test error")
    }

    fun goBack() {
        navigateBack()
    }
}
