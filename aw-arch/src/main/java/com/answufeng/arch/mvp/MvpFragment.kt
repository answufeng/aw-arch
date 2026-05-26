package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment

/**
 * MVP 架构 Fragment 基类
 *
 * 适用于传统 MVP 模式的 Fragment，提供 ViewBinding 支持、Presenter 自动创建与生命周期绑定，并集成懒加载。
 * Presenter 由 [androidx.lifecycle.ViewModel] 持有，在配置变更与 [onDestroyView] 后不会丢失状态。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. attach presenter → 3. [initView] → 4. [initObservers] → 5. [onLazyLoad]（首次可见）
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Fragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : BaseFragment<VB>(), MvpView {
    private var archInjectedPresenter: P? = null

    private val presenterHolder: MvpPresenterHolder<P> by viewModels {
        MvpPresenterViewModelFactory { createPresenter() }
    }

    /** 当前 Presenter；Hilt 子类可 `override val presenter`。 */
    protected open val presenter: P
        get() = archInjectedPresenter ?: presenterHolder.presenter

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        injectPresenter()?.let { archInjectedPresenter = it }
        presenter.attachView(contractView)
        super.onViewCreated(view, savedInstanceState)
    }

    /** Hilt 注入；见 [com.answufeng.arch.hilt.HiltMvpFragment]。 */
    protected open fun injectPresenter(): P? = null

    protected open fun createPresenter(): P = reflectiveCreatePresenter(javaClass)

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        presenter.detachView()
        archInjectedPresenter = null
        super.onDestroyView()
    }
}
