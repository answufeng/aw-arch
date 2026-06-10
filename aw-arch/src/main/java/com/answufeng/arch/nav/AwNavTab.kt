package com.answufeng.arch.nav

/**
 * @deprecated 使用 [TabConfig] 替代。Tab 管理已内置到 [AwNav]，
 * 不再需要 [AwNavTabSwitcher]。迁移方式：
 * ```kotlin
 * // 旧
 * val tabSwitcher = nav.tabSwitcher(listOf(AwNavTab("a", rootRoute = "home")))
 * tabSwitcher.selectTab("a")
 *
 * // 新
 * nav.initTabs(TabConfig("a", rootRoute = "home"))
 * nav.switchTab("a")
 * ```
 */
@Deprecated(
    message = "Use TabConfig + AwNav.initTabs() instead. Tab management is now built into AwNav.",
    replaceWith = ReplaceWith("TabConfig(id, rootRoute)"),
)
data class AwNavTab(
    val id: String,
    val rootRoute: String = "",
)
