package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

/**
 * Hilt 支持的 MVP 架构 Fragment 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVP 模式 Fragment，提供 ViewBinding 支持、Presenter 生命周期绑定与懒加载。
 * 继承 [BaseFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 Fragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class HiltMvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> :
    BaseFragment<VB>(), MvpView {

    abstract val presenter: P

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenter.attachView(contractView)
        super.onViewCreated(view, savedInstanceState)
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
