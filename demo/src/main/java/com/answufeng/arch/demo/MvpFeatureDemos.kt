package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.answufeng.arch.demo.databinding.ActivityMvpDemoBinding
import com.answufeng.arch.mvp.BaseMvpPresenter
import com.answufeng.arch.mvp.MvpActivity
import kotlinx.coroutines.delay

interface MvpSingleActionContract {
    interface View : com.answufeng.arch.mvp.MvpView {
        fun renderCount(count: Int)
    }
}

class MvpSingleActionPresenter : BaseMvpPresenter<MvpSingleActionContract.View>() {
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
        count = 7
        withMain {
            viewOrNull?.onLoading(false)
            viewOrNull?.renderCount(count)
            viewOrNull?.showToast("MVP: loaded = 7")
        }
    }
}

abstract class BaseMvpSingleActionActivity(
    private val setup: (ActivityMvpDemoBinding, MvpSingleActionPresenter) -> Unit
) : MvpActivity<ActivityMvpDemoBinding, MvpSingleActionContract.View, MvpSingleActionPresenter>(),
    MvpSingleActionContract.View {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityMvpDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 全部按钮先置灰，交给子类打开单一动作
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

class MvpIncrementDemoActivity : BaseMvpSingleActionActivity(setup = { b, p ->
    b.btnIncrement.isEnabled = true
    b.btnIncrement.setOnClickListener { p.increment() }
})

class MvpDecrementDemoActivity : BaseMvpSingleActionActivity(setup = { b, p ->
    b.btnDecrement.isEnabled = true
    b.btnDecrement.setOnClickListener { p.decrement() }
})

class MvpResetDemoActivity : BaseMvpSingleActionActivity(setup = { b, p ->
    b.btnReset.isEnabled = true
    b.btnReset.setOnClickListener { p.reset() }
})

class MvpLoadDataDemoActivity : BaseMvpSingleActionActivity(setup = { b, p ->
    b.btnLoadData.isEnabled = true
    b.btnLoadData.setOnClickListener { p.loadData() }
})

