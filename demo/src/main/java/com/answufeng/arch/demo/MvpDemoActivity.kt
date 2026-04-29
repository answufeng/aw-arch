package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.answufeng.arch.demo.databinding.ActivityMvpDemoBinding
import com.answufeng.arch.mvp.MvpActivity

class MvpDemoActivity : MvpActivity<ActivityMvpDemoBinding, MvpCounterContract.View, MvpCounterPresenter>(),
    MvpCounterContract.View {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvpDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.btnIncrement.setOnClickListener { presenter.increment() }
        binding.btnDecrement.setOnClickListener { presenter.decrement() }
        binding.btnReset.setOnClickListener { presenter.reset() }
        binding.btnLoadData.setOnClickListener { presenter.loadData() }
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun renderCount(count: Int) {
        binding.tvCount.text = count.toString()
    }
}
