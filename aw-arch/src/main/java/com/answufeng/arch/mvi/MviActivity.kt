package com.answufeng.arch.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * MVI 模式的 Activity 基类。
 *
 * 提供：
 * - ViewBinding 自动绑定
 * - ViewModel 自动创建
 * - UI 状态和事件的收集
 *
 * ### 子类实现示例
 * ```kotlin
 * class LoginActivity : MviActivity<ActivityLoginBinding, LoginState, LoginEvent, LoginIntent, LoginViewModel>() {
 *     override fun viewModelClass() = LoginViewModel::class.java
 *
 *     override fun inflateBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)
 *
 *     override fun initView(savedInstanceState: Bundle?) {
 *         binding.btnLogin.setOnClickListener { dispatch(LoginIntent.Login) }
 *     }
 *
 *     override fun render(state: LoginState) {
 *         binding.progressBar.isVisible = state.isLoading
 *     }
 *
 *     override fun handleEvent(event: LoginEvent) {
 *         when (event) {
 *             is LoginEvent.LoginSuccess -> navigateToHome()
 *             is LoginEvent.LoginFailure -> showToast(event.message)
 *         }
 *     }
 * }
 * ```
 */
abstract class MviActivity<VB : ViewBinding, STATE : UiState, EVENT : UiEvent, INTENT : UiIntent, VM : MviViewModel<STATE, EVENT, INTENT>> : AppCompatActivity() {

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
        observeState()
        observeEvents()
        initView(savedInstanceState)
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    /** 初始化 View */
    abstract fun initView(savedInstanceState: Bundle?)

    /** 渲染 UI 状态 */
    abstract fun render(state: STATE)

    /** 处理 UI 事件 */
    abstract fun handleEvent(event: EVENT)

    /** 创建 ViewModel */
    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    /** 观察 UI 状态 */
    private fun observeState() {
        coroutineScope.launch {
            viewModel.state.collect { render(it) }
        }
    }

    /** 观察 UI 事件 */
    private fun observeEvents() {
        coroutineScope.launch {
            viewModel.event.collect { handleEvent(it) }
        }
    }

    /** 分发 UI 意图 */
    protected fun dispatch(intent: INTENT) {
        viewModel.dispatch(intent)
    }
}