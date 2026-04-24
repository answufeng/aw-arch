package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 支持的 MVP 架构 Activity 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVP 模式 Activity，提供 ViewBinding 支持与 Presenter 生命周期绑定。
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Activity 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class HiltMvpActivity<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    AppCompatActivity(), MvpView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    abstract val presenter: P

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        presenter.attachView(contractView)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

    override fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
        _binding = null
    }
}

