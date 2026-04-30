package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.ext.inferPresenterClass

/**
 * MVP 架构 Fragment 基类
 *
 * 适用于传统 MVP 模式的 Fragment，提供 ViewBinding 支持、Presenter 自动创建与生命周期绑定，并集成懒加载。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. attach presenter → 3. [initView] → 4. [initObservers] → 5. [onLazyLoad]（首次可见）
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Fragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : BaseFragment<VB>(), MvpView {

    protected lateinit var presenter: P

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenter = createPresenter()
        presenter.attachView(contractView)
        super.onViewCreated(view, savedInstanceState)
    }

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
    }
}
