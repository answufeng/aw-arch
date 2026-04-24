package com.answufeng.arch.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.ext.inferPresenterClass

/**
 * MVP 架构 DialogFragment 基类
 *
 * @param VB ViewBinding 类型
 * @param V View Contract 类型，必须实现 [MvpView]（通常由 DialogFragment 自身实现）
 * @param P Presenter 类型，必须实现 [MvpPresenter]
 */
abstract class MvpDialogFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : DialogFragment(), MvpView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    protected lateinit var presenter: P

    @Suppress("UNCHECKED_CAST")
    protected val contractView: V get() = this as V

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {}

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
        dismiss()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
        _binding = null
    }
}

