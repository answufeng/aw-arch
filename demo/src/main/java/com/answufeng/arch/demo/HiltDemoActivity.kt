package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.answufeng.arch.demo.databinding.ActivityHiltDemoBinding
import com.answufeng.arch.hilt.HiltMvvmActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltDemoActivity : HiltMvvmActivity<ActivityHiltDemoBinding, HiltDemoViewModel>() {

    override val viewModel: HiltDemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityHiltDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnPing.setOnClickListener { viewModel.showHiltMessage() }
    }
}
