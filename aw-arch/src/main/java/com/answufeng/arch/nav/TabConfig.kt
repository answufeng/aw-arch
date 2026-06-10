package com.answufeng.arch.nav

/**
 * Tab 配置，用于 [AwNav.initTabs]。
 *
 * ```kotlin
 * nav.initTabs(
 *     TabConfig("home", rootRoute = "home"),
 *     TabConfig("contact", rootRoute = "contact", switchAnim = NavAnim.FADE),
 *     TabConfig("discover", rootRoute = "discover"),
 *     TabConfig("profile", rootRoute = "profile"),
 * )
 * ```
 *
 * @param id         Tab 唯一标识，同时作为内部栈的 key
 * @param rootRoute  根页面路由名（必须已通过 [AwNav.register] 注册）
 * @param switchAnim Tab 切换时的转场动画，默认无动画
 */
data class TabConfig(
    val id: String,
    val rootRoute: String,
    val switchAnim: NavAnim = NavAnim.NONE,
)
