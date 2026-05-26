package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding

/**
 * MVP 架构 DialogFragment 基类
 *
 * Presenter 由 ViewModel 持有，避免 [onDestroyView] 后重建导致状态丢失。
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 DialogFragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpDialogFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : DialogFragment(), MvpView {
    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    private var archInjectedPresenter: P? = null

    private val presenterHolder: MvpPresenterHolder<P> by viewModels {
        MvpPresenterViewModelFactory { createPresenter() }
    }

    protected open val presenter: P
        get() = archInjectedPresenter ?: presenterHolder.presenter

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    abstract fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        injectPresenter()?.let { archInjectedPresenter = it }
        presenter.attachView(contractView)
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

    protected open fun injectPresenter(): P? = null

    protected open fun createPresenter(): P = reflectiveCreatePresenter(javaClass)

    override fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        dismiss()
    }

    override fun onDestroyView() {
        presenter.detachView()
        archInjectedPresenter = null
        super.onDestroyView()
        _binding = null
    }
}
