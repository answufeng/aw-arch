package com.answufeng.arch.nav

import androidx.fragment.app.Fragment

/**
 * Fragment 实现此接口可拦截返回键。
 *
 * 典型场景：列表页先滚动到顶部，再按返回才退出。
 *
 * ```kotlin
 * class ProductsFragment : Fragment(), OnGoBackListener {
 *     override fun onGoBack(): Boolean {
 *         if (shouldScrollToTop()) {
 *             scrollToTop()
 *             return false  // 拦截，不返回
 *         }
 *         return true  // 允许返回
 *     }
 * }
 * ```
 */
interface OnGoBackListener {
    /** 返回 `true` 允许返回，`false` 拦截返回事件。 */
    fun onGoBack(): Boolean
}
