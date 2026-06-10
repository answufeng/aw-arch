package com.answufeng.arch.nav

/**
 * Fragment 实现此接口可指定自己的管理策略，覆盖默认的 [NavigatorTransaction.ATTACH_DETACH]。
 *
 * 适合 WebView、地图等需要保留 View 状态的页面：
 *
 * ```kotlin
 * class WebViewFragment : Fragment(), OnNavigatorTransactionListener {
 *     override val navigatorTransaction = NavigatorTransaction.SHOW_HIDE
 * }
 * ```
 */
interface OnNavigatorTransactionListener {
    val navigatorTransaction: NavigatorTransaction
}
