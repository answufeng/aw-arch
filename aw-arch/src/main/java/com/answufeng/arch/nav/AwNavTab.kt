package com.answufeng.arch.nav

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentManager

/**
 * 单个 BottomNav Tab 的配置。
 *
 * @param id Tab 唯一标识（与 BottomNavigation item 对应）
 * @param rootRoute 该 Tab 根页面路由（须已在 [AwNav] 注册）
 * @param backStackName [FragmentManager.saveBackStack] / [restoreBackStack] 使用的名称
 */
data class AwNavTab(
    val id: String,
    val rootRoute: String,
    val backStackName: String = "aw_tab_stack_$id",
)

/**
 * 基于 AndroidX [FragmentManager.saveBackStack] / [restoreBackStack] 的多 Tab 返回栈切换器。
 *
 * 与 [AwNav] 共用同一容器；Tab 内多级仍用 [AwNav.navigate]。
 * Tab 根页面首次进入时请通过 [selectTab] 导航（内部会 `addToBackStack(backStackName)`）。
 */
class AwNavTabSwitcher internal constructor(
    private val nav: AwNav,
    private val fragmentManager: FragmentManager,
    private val containerId: Int,
    tabs: List<AwNavTab>,
) {
    private val tabById = tabs.associateBy { it.id }

    var selectedTabId: String? = null
        private set

    init {
        require(tabs.isNotEmpty()) { "AwNavTabSwitcher requires at least one tab" }
        require(tabById.size == tabs.size) { "Duplicate AwNavTab.id" }
    }

    @MainThread
    fun selectTab(
        tabId: String,
        args: Bundle? = null,
        rootNavOptions: (NavOptions.() -> Unit)? = null,
    ) {
        val tab = tabById[tabId] ?: throw IllegalArgumentException("Unknown tab id: $tabId")
        val previous = selectedTabId
        if (previous == tabId) return

        if (fragmentManager.isStateSaved) {
            com.answufeng.arch.config.AwArch.logger.w(
                "AwNavTabSwitcher",
                "selectTab(\"$tabId\") ignored: FragmentManager state already saved",
            )
            return
        }

        previous?.let { prevId ->
            val prev = tabById[prevId]!!
            fragmentManager.saveBackStack(prev.backStackName)
        }

        selectedTabId = tabId
        fragmentManager.restoreBackStack(tab.backStackName)
        fragmentManager.executePendingTransactions()
        nav.syncCurrentRoute()

        val visible = fragmentManager.findFragmentById(containerId)
        if (visible == null || visible.tag != tab.rootRoute) {
            nav.navigate(tab.rootRoute, args) {
                addToBackStack = true
                backStackName = tab.backStackName
                anim = NavAnim.NONE
                rootNavOptions?.invoke(this)
            }
        }
    }

    fun saveState(outState: Bundle) {
        selectedTabId?.let { outState.putString(KEY_SELECTED_TAB_ID, it) }
    }

    /**
     * 在 Activity `onCreate` 中、`selectTab` 之前调用，恢复进程重建前的选中 Tab。
     * FragmentManager 会自行恢复各 Tab 返回栈。
     */
    @MainThread
    fun restoreState(savedInstanceState: Bundle?) {
        val tabId = savedInstanceState?.getString(KEY_SELECTED_TAB_ID) ?: return
        if (tabId !in tabById) return
        selectTab(tabId)
    }

    private companion object {
        private const val KEY_SELECTED_TAB_ID = "aw_arch_nav_selected_tab_id"
    }
}
