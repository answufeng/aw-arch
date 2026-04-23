package com.answufeng.arch.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Fragment 基类，自动管理 ViewBinding 生命周期并支持懒加载。
 *
 * 生命周期回调顺序：
 * 1. [inflateBinding] → 2. [initView] → 3. [initObservers] → 4. [onLazyLoad]（首次可见时）
 *
 * ```kotlin
 * class HomeFragment : BaseFragment<FragmentHomeBinding>() {
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         FragmentHomeBinding.inflate(inflater, container, false)
 *
 *     override fun initView(savedInstanceState: Bundle?) {
 *         binding.tvTitle.text = "Hello"
 *     }
 *
 *     override fun onLazyLoad() {
 *         viewModel.loadData()
 *     }
 * }
 * ```
 *
 * @param VB ViewBinding 类型
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    /**
     * 当前 ViewBinding 实例。
     *
     * 仅在 [onCreateView]–[onDestroyView] 之间有效，
     * 其他时刻访问抛出 [IllegalStateException]。
     */
    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not accessible before onCreateView or after onDestroyView")

    private val lazyLoadHelper = LazyLoadHelper(this)

    /** 由子类实现：创建 ViewBinding 实例 */
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /** 初始化视图 */
    abstract fun initView(savedInstanceState: Bundle?)

    /** 可选：初始化数据观察 */
    open fun initObservers() {}

    /**
     * 懒加载回调：每次 [onCreateView] 创建新 View 后，首次 [onResume] 触发一次（自 [LazyLoadHelper.prepareForNewView] 起算）。
     *
     * 适合在此处发起首次数据请求。
     */
    open fun onLazyLoad() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyLoadHelper.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        lazyLoadHelper.prepareForNewView()
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        if (lazyLoadHelper.shouldLazyLoad()) {
            onLazyLoad()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lazyLoadHelper.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
