package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseViewModel
import com.answufeng.arch.base.BaseViewModel.UIEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * MVVM 模式的 Activity 基类。
 *
 * 提供：
 * - ViewBinding 自动绑定
 * - ViewModel 自动创建
 * - UI 事件的收集和处理
 *
 * ### 子类实现示例
 * ```kotlin
 * class HomeActivity : MvvmActivity<ActivityHomeBinding, HomeViewModel>() {
 *     override fun viewModelClass() = HomeViewModel::class.java
 *
 *     override fun inflateBinding(inflater: LayoutInflater) = ActivityHomeBinding.inflate(inflater)
 *
 *     override fun initView(savedInstanceState: Bundle?) {
 *         binding.btnLoad.setOnClickListener { viewModel.loadData() }
 *     }
 *
 *     override fun onUIEvent(event: UIEvent) {
 *         when (event) {
 *             is UIEvent.Toast -> showToast(event.message)
 *             is UIEvent.Navigate -> navigateTo(event.route)
 *             else -> super.onUIEvent(event)
 *         }
 *     }
 * }
 * ```
 */
abstract class MvvmActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    protected lateinit var viewModel: VM
    protected lateinit var binding: VB

    private val coroutineScope = MainScope()

    /** ViewModel 类 */
    abstract fun viewModelClass(): Class<VM>

    /** 加载 ViewBinding */
    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel()
        observeUIEvents()
        initView(savedInstanceState)
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    /** 初始化 View */
    abstract fun initView(savedInstanceState: Bundle?)

    /** 处理 UI 事件 */
    open fun onUIEvent(event: UIEvent) {
        when (event) {
            is UIEvent.Toast -> showToast(event.message)
            is UIEvent.Loading -> onLoading(event.show)
            is UIEvent.Navigate -> navigateTo(event.route, event.extras)
            is UIEvent.NavigateBack -> navigateBack()
            is UIEvent.Custom -> handleCustomEvent(event.key, event.data)
        }
    }

    /** 显示/隐藏 Loading */
    open fun onLoading(show: Boolean) {
        // 子类可覆写实现全局 Loading
    }

    /** 创建 ViewModel */
    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    /** 观察 UI 事件 */
    private fun observeUIEvents() {
        coroutineScope.launch {
            viewModel.uiEvent.collect { onUIEvent(it) }
        }
    }

    /** 显示 Toast */
    protected open fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    /** 导航到指定路由 */
    protected open fun navigateTo(route: String, extras: Map<String, Any>? = null) {
        // 子类可覆写实现导航逻辑
    }

    /** 返回上一页 */
    protected open fun navigateBack() {
        finish()
    }

    /** 处理自定义事件 */
    protected open fun handleCustomEvent(key: String, data: Any?) {
        // 子类可覆写处理自定义事件
    }
}