package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UiEvent
import com.answufeng.arch.ext.inferViewModelClass
import kotlinx.coroutines.launch

/**
 * MVVM 架构 Activity 基类
 *
 * 适用于传统 MVVM 模式的 Activity，提供了 ViewBinding 支持、ViewModel 自动创建和 UI 事件处理。
 * ViewModel 会根据子类类型自动推断创建。
 *
 * 与 [MviActivity] 的区别：MVVM 模式更简单，不需要定义 State/Event/Intent，
 * 适合不需要严格单向数据流的场景。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. [initView] → 3. [initObservers]
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 */
abstract class MvvmActivity<VB : ViewBinding, VM : MvvmViewModel> : AppCompatActivity(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    protected lateinit var viewModel: VM

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { onUiEvent(it) }
            }
        }
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass<VM>(javaClass, MvvmViewModel::class.java)
        @Suppress("UNCHECKED_CAST")
        return ViewModelProvider(this).get(vmClass) as VM
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
