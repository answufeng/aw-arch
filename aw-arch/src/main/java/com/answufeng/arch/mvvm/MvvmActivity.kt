package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseActivity
import com.answufeng.arch.base.BaseViewModel

/**
 * MVVM Activity 基类。
 *
 * 子类通过 [viewModelClass] 提供 ViewModel 类型，基类自动创建实例并收集 [BaseViewModel.uiEvent]。
 *
 * ### 非 Hilt 用法
 * ```kotlin
 * class HomeActivity : MvvmActivity<ActivityHomeBinding, HomeViewModel>() {
 *     override fun viewModelClass() = HomeViewModel::class.java
 *     override fun inflateBinding(inflater: LayoutInflater) =
 *         ActivityHomeBinding.inflate(inflater)
 *     override fun initView(savedInstanceState: Bundle?) { ... }
 * }
 * ```
 *
 * ### Hilt 用法（推荐）
 * ```kotlin
 * @AndroidEntryPoint
 * class HomeActivity : MvvmActivity<ActivityHomeBinding, HomeViewModel>() {
 *     override fun viewModelClass() = HomeViewModel::class.java
 *     override fun inflateBinding(inflater: LayoutInflater) =
 *         ActivityHomeBinding.inflate(inflater)
 *     override fun initView(savedInstanceState: Bundle?) { ... }
 * }
 *
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val repository: HomeRepository
 * ) : BaseViewModel() { ... }
 * ```
 *
 * > **注意**：使用 Hilt 时，Activity 必须添加 `@AndroidEntryPoint` 注解，
 * > ViewModel 必须添加 `@HiltViewModel` 和 `@Inject` 构造函数。
 * > 基类会自动检测 Hilt 环境，无需额外配置。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 */
abstract class MvvmActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>(), MvvmView<VM> {

    protected lateinit var viewModel: VM
        private set

    override val mvvmViewModel: VM get() = viewModel

    override val viewContext: Context get() = this

    /** 返回 ViewModel 的 Class 对象，子类必须实现 */
    abstract fun viewModelClass(): Class<VM>

    /**
     * 创建 ViewModel 实例。
     *
     * 默认使用 [ViewModelProvider] + [viewModelClass] 创建。
     * 当 Activity 添加了 `@AndroidEntryPoint` 且 ViewModel 添加了 `@HiltViewModel` 时，
     * Hilt 会自动通过 [defaultViewModelProviderFactory] 注入依赖。
     */
    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    override fun onPreInit(savedInstanceState: Bundle?) {
        viewModel = createViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectUIEvents()
    }

    override fun onNavigateBack() {
        finish()
    }
}
