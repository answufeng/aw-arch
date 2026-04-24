package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferPresenterClass

/**
 * MVP 架构 Activity 基类
 *
 * 适用于传统 MVP 模式的 Activity，提供 ViewBinding 支持、Presenter 自动创建与生命周期绑定。
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

    protected lateinit var presenter: P

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        presenter = createPresenter()
        presenter.attachView(contractView)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

    protected open fun createPresenter(): P {
        val pClass = inferPresenterClass<P>(javaClass, MvpPresenter::class.java)
        val ctor = pClass.getDeclaredConstructor()
        ctor.isAccessible = true
        return ctor.newInstance()
    }

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

