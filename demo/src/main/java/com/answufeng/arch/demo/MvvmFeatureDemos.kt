package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.demo.databinding.ActivityMvvmDemoBinding
import com.answufeng.arch.mvvm.MvvmActivity
import kotlinx.coroutines.delay

class MvvmLoadingViewModel : MvvmViewModel() {
    fun load() = launchIO {
        showLoading(true)
        delay(800)
        showLoading(false)
        showToast("加载完成")
    }
}

class MvvmLoadingDemoActivity : MvvmActivity<ActivityMvvmDemoBinding, MvvmLoadingViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvvmDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnLoadData.setOnClickListener { viewModel.load() }
        binding.btnShowToast.isEnabled = false
        binding.btnError.isEnabled = false
        binding.btnNavigateBack.setOnClickListener { finish() }
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}

class MvvmToastViewModel : MvvmViewModel() {
    fun toast() = showToast("Hello from MVVM Toast")
}

class MvvmToastDemoActivity : MvvmActivity<ActivityMvvmDemoBinding, MvvmToastViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvvmDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnShowToast.setOnClickListener { viewModel.toast() }
        binding.btnLoadData.isEnabled = false
        binding.btnError.isEnabled = false
        binding.btnNavigateBack.setOnClickListener { finish() }
    }
}

class MvvmErrorViewModel : MvvmViewModel() {
    fun crash() = launch { error("MVVM test error") }
}

class MvvmErrorDemoActivity : MvvmActivity<ActivityMvvmDemoBinding, MvvmErrorViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvvmDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnError.setOnClickListener { viewModel.crash() }
        binding.btnLoadData.isEnabled = false
        binding.btnShowToast.isEnabled = false
        binding.btnNavigateBack.setOnClickListener { finish() }
    }
}

class MvvmBackViewModel : MvvmViewModel() {
    fun back() = navigateBack()
}

class MvvmBackDemoActivity : MvvmActivity<ActivityMvvmDemoBinding, MvvmBackViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvvmDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnNavigateBack.setOnClickListener { viewModel.back() }
        binding.btnLoadData.isEnabled = false
        binding.btnShowToast.isEnabled = false
        binding.btnError.isEnabled = false
    }
}

