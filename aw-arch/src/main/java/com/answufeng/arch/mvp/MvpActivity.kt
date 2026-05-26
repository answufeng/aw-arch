package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * MVP 架构 Activity 基类
 *
 * 适用于传统 MVP 模式的 Activity，提供 ViewBinding 支持、Presenter 自动创建与生命周期绑定。
 * Presenter 由 [androidx.lifecycle.ViewModel] 持有，配置变更后不会丢失状态。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. attach presenter → 3. [initView] → 4. [initObservers]
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Activity 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpActivity<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : AppCompatActivity(), MvpView {
    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    private var archInjectedPresenter: P? = null

    private val presenterHolder: MvpPresenterHolder<P> by viewModels {
        MvpPresenterViewModelFactory { createPresenter() }
    }

    /** 当前 Presenter；Hilt 子类可 `override val presenter` 并配合 [injectPresenter]。 */
    protected open val presenter: P
        get() = archInjectedPresenter ?: presenterHolder.presenter

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        injectPresenter()?.let { archInjectedPresenter = it }
        presenter.attachView(contractView)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

    /** Hilt 注入；见 [com.answufeng.arch.hilt.HiltMvpActivity]。 */
    protected open fun injectPresenter(): P? = null

    protected open fun createPresenter(): P = reflectiveCreatePresenter(javaClass)

    override fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun onDestroy() {
        presenter.detachView()
        archInjectedPresenter = null
        super.onDestroy()
        _binding = null
    }
}
