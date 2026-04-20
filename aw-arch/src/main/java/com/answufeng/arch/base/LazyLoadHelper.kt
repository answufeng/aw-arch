package com.answufeng.arch.base

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment 懒加载辅助类，确保 [Fragment] 首次可见时只执行一次加载逻辑。
 *
 * 在进程重启（如配置变更）后通过 [Bundle] 恢复 [isFirstLoad] 状态，
 * 避免重复触发懒加载。
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
     * 判断是否需要执行懒加载。
     *
     * 首次调用返回 `true` 并将内部标记置为 `false`，后续调用均返回 `false`。
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
