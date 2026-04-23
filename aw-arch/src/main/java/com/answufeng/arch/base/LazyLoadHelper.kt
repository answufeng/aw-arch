package com.answufeng.arch.base

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment 懒加载辅助类：在 **每次新建 View** 后的首次 [Fragment.onResume] 触发一次 [onLazyLoad]。
 *
 * 须在 [Fragment.onCreateView] 开头调用 [prepareForNewView]（各 [BaseFragment]/[MviFragment] 等基类已接入）。
 * 否则从返回栈恢复、配置变更等导致 View 重建时，若仍沿用「整实例只加载一次」会 **不再调用 onLazyLoad**，易出现空白界面。
 *
 * [onSaveInstanceState] 仍会保存当前是否已对本段 View 执行过懒加载；[prepareForNewView] 会在新 View 创建时重置，
 * 与进程恢复组合后仍能正确再加载一次。
 *
 * 典型用法：
 * ```kotlin
 * class MyFragment : Fragment() {
 *     private val lazyLoadHelper = LazyLoadHelper(this)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         lazyLoadHelper.onCreate(savedInstanceState)
 *     }
 *
 *     override fun onResume() {
 *         super.onResume()
 *         if (lazyLoadHelper.shouldLazyLoad()) {
 *             // 首次可见，执行数据加载
 *         }
 *     }
 *
 *     override fun onSaveInstanceState(outState: Bundle) {
 *         super.onSaveInstanceState(outState)
 *         lazyLoadHelper.onSaveInstanceState(outState)
 *     }
 * }
 * ```
 *
 * @param fragment 关联的 Fragment 实例
 */
class LazyLoadHelper(private val fragment: Fragment) {

    private var isFirstLoad = true

    companion object {
        private const val KEY_IS_FIRST_LOAD = "aw_lazy_load_is_first_load"
    }

    /**
     * 在 [Fragment.onCreate] 中调用，从 [savedInstanceState] 恢复懒加载状态。
     *
     * @param savedInstanceState Fragment 保存的实例状态，可为 null
     */
    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isFirstLoad = savedInstanceState.getBoolean(KEY_IS_FIRST_LOAD, true)
        }
    }

    /**
     * 在 [Fragment.onCreateView] 中、创建 ViewBinding **之前**调用。
     * 表示即将挂载新 View，下一次 [onResume] 应再次允许懒加载（若尚未消费）。
     */
    fun prepareForNewView() {
        isFirstLoad = true
    }

    /**
     * 判断是否需要执行懒加载。
     *
     * 自上一次 [prepareForNewView] 后首次调用返回 `true` 并将内部标记置为 `false`，直至下次 [prepareForNewView]。
     *
     * @return 首次调用返回 `true`，否则返回 `false`
     */
    fun shouldLazyLoad(): Boolean {
        if (isFirstLoad) {
            isFirstLoad = false
            return true
        }
        return false
    }

    /**
     * 在 [Fragment.onSaveInstanceState] 中调用，保存懒加载状态以防进程重启后重复加载。
     *
     * @param outState 即将保存的 Bundle
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_FIRST_LOAD, isFirstLoad)
    }
}
