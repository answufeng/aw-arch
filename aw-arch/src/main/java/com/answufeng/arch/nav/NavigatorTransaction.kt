package com.answufeng.arch.nav

/**
 * Fragment 管理策略，决定非活跃页面如何从 UI 移除。
 *
 * - [ATTACH_DETACH]：默认。detach 时销毁 View 但保留 Fragment 实例，内存友好。
 *   生命周期回调：detach → onPause/onStop，attach → onStart/onResume。
 * - [SHOW_HIDE]：不触发生命周期回调，View 保留在内存。
 *   适合 WebView、地图等需要保留复杂 View 状态的页面。
 *
 * Fragment 可通过实现 [OnNavigatorTransactionListener] 指定自己的策略。
 */
enum class NavigatorTransaction {
    ATTACH_DETACH,
    SHOW_HIDE,
}
