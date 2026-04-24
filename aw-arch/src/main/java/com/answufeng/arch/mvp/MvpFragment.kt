package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.LazyLoadHelper
import com.answufeng.arch.ext.inferPresenterClass

/**
 * MVP 架构 Fragment 基类
 *
 * 适用于传统 MVP 模式的 Fragment，提供 ViewBinding 支持、Presenter 自动创建与生命周期绑定，并集成懒加载。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. attach presenter → 3. [initView] → 4. [initObservers] → 5. [onLazyLoad]（首次可见）
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Fragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : Fragment(), MvpView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var presenter: P

    private val lazyLoadHelper = LazyLoadHelper(this)

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyLoadHelper.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        lazyLoadHelper.prepareForNewView()
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = createPresenter()
        presenter.attachView(contractView)
        initView(savedInstanceState)
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        if (lazyLoadHelper.shouldLazyLoad()) {
            onLazyLoad()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lazyLoadHelper.onSaveInstanceState(outState)
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

    open fun onLazyLoad() {}

    protected open fun createPresenter(): P {
        val pClass = inferPresenterClass<P>(javaClass, MvpPresenter::class.java)
        val ctor = pClass.getDeclaredConstructor()
        ctor.isAccessible = true
        return ctor.newInstance()
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
        _binding = null
    }
}

