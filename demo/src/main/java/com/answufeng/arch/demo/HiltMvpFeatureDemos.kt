package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.answufeng.arch.demo.databinding.ActivityHiltMvpDemoBinding
import com.answufeng.arch.hilt.HiltMvpActivity
import com.answufeng.arch.mvp.BaseMvpPresenter
import com.answufeng.arch.mvp.MvpView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

interface HiltMvpSingleActionContract {
    interface View : MvpView {
        fun renderCount(count: Int)
    }
}

class HiltMvpSingleActionPresenter @Inject constructor() : BaseMvpPresenter<HiltMvpSingleActionContract.View>() {
    private var count: Int = 0

    fun increment() {
        count++
        viewOrNull?.renderCount(count)
    }

    fun decrement() {
        count--
        viewOrNull?.renderCount(count)
    }

    fun reset() {
        count = 0
        viewOrNull?.renderCount(count)
    }

    fun loadData() = launchIO {
        withMain { viewOrNull?.onLoading(true) }
        delay(600)
        count = 77
        withMain {
            viewOrNull?.onLoading(false)
            viewOrNull?.renderCount(count)
            viewOrNull?.showToast("Hilt MVP: loaded = 77")
        }
    }
}

@AndroidEntryPoint
abstract class BaseHiltMvpSingleActionActivity(
    private val setup: (ActivityHiltMvpDemoBinding, HiltMvpSingleActionPresenter) -> Unit
) : HiltMvpActivity<ActivityHiltMvpDemoBinding, HiltMvpSingleActionContract.View, HiltMvpSingleActionPresenter>(),
    HiltMvpSingleActionContract.View {

    @Inject lateinit var injectedPresenter: HiltMvpSingleActionPresenter
    override val presenter: HiltMvpSingleActionPresenter get() = injectedPresenter

    override fun inflateBinding(inflater: LayoutInflater) = ActivityHiltMvpDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnIncrement.isEnabled = false
        binding.btnDecrement.isEnabled = false
        binding.btnReset.isEnabled = false
        binding.btnLoadData.isEnabled = false
        binding.btnBack.setOnClickListener { finish() }

        setup(binding, presenter)
        renderCount(0)
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun renderCount(count: Int) {
        binding.tvCount.text = count.toString()
    }
}

class HiltMvpIncrementDemoActivity : BaseHiltMvpSingleActionActivity(setup = { b, p ->
    b.btnIncrement.isEnabled = true
    b.btnIncrement.setOnClickListener { p.increment() }
})

class HiltMvpDecrementDemoActivity : BaseHiltMvpSingleActionActivity(setup = { b, p ->
    b.btnDecrement.isEnabled = true
    b.btnDecrement.setOnClickListener { p.decrement() }
})

class HiltMvpResetDemoActivity : BaseHiltMvpSingleActionActivity(setup = { b, p ->
    b.btnReset.isEnabled = true
    b.btnReset.setOnClickListener { p.reset() }
})

class HiltMvpLoadDataDemoActivity : BaseHiltMvpSingleActionActivity(setup = { b, p ->
    b.btnLoadData.isEnabled = true
    b.btnLoadData.setOnClickListener { p.loadData() }
})

