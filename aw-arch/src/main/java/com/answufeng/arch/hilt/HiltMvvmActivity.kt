package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.mvvm.MvvmView
import com.answufeng.arch.mvvm.dispatchMvvmUiEvent
import com.answufeng.arch.nav.AwNav
import kotlinx.coroutines.launch

/**
 * Hilt 支持的 MVVM 架构 Activity 基类
 *
 * 适用于使用 Hilt 进行依赖注入的 MVVM 模式 Activity，提供了 ViewBinding 支持和 UI 事件处理
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型，必须继承 [MvvmViewModel]
 *
 * @see MvvmViewModel
 * @see MvvmView
 */
abstract class HiltMvvmActivity<VB : ViewBinding, VM : MvvmViewModel> : AppCompatActivity(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    /**
     * 若返回非 null，[com.answufeng.arch.base.MvvmViewModel.UiEvent.Navigate] 等将交给 [AwNav]。
     */
    protected open val awNav: AwNav? get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    abstract val viewModel: VM

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    dispatchMvvmUiEvent(event, awNav) { navigateBack() }
                }
            }
        }
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
