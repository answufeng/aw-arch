package com.answufeng.arch.nav

import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import androidx.activity.OnBackPressedCallback
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.answufeng.arch.R
import com.answufeng.arch.config.AwArch
import com.answufeng.arch.config.AwArchDsl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

/**
 * 轻量级 Fragment 导航控制器，基于 `add + attach/detach` 管理，不依赖 FM BackStack。
 *
 * 核心设计：
 * - 使用 `add` + `attach/detach`（或 `show/hide`）管理页面，Fragment 实例不会被重建
 * - 自维护 `tabStacks`，每个 Tab 独立返回栈
 * - Tab 切换只做 `detach/attach`，页面状态完整保留
 * - 支持进程重建后自动恢复栈状态
 *
 * ```kotlin
 * val nav = AwNav.init(this, R.id.container).apply {
 *     register {
 *         route<HomeFragment>("home")
 *         route<DetailFragment>("detail")
 *     }
 *     addInterceptor(LoginInterceptor())
 * }
 *
 * nav.navigate("detail", Bundle().apply { putInt("id", 42) })
 * ```
 */
class AwNav private constructor(
    private val activityRef: WeakReference<FragmentActivity>,
    private val hostFragmentRef: WeakReference<Fragment>?,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val instanceKey: Long,
) {
    // ===== 路由与拦截器 =====

    private val routes = mutableMapOf<String, KClass<out Fragment>>()
    private val interceptors = mutableListOf<NavInterceptor>()

    // ===== 自维护 Fragment 栈 =====

    /** 每个 Tab 的 Fragment 栈，key = tabId */
    private val tabStacks = mutableMapOf<String, MutableList<StackEntry>>()

    /** Tab 配置，key = tabId */
    private val tabConfigs = mutableMapOf<String, TabConfig>()

    /** 当前活跃 Tab ID */
    private var activeTabId: String = DEFAULT_TAB

    /** 重复选择当前 Tab 的回调 */
    var onTabReselect: ((tabId: String) -> Unit)? = null

    /** 栈条目 */
    data class StackEntry(
        val route: String,
        val fragment: Fragment,
        val tabId: String,
        val groupName: String? = null,
        val navigatorTransaction: NavigatorTransaction = NavigatorTransaction.ATTACH_DETACH,
        val animSet: AnimSet? = null,
    )

    // ===== 状态 =====

    private var _currentRoute: String? = null

    private val _routeFlow = MutableStateFlow<String?>(null)

    /** 响应式观察当前路由变化。 */
    val currentRouteFlow: StateFlow<String?> = _routeFlow.asStateFlow()

    private val lastNavigateTimeByRoute = ConcurrentHashMap<String, AtomicLong>()

    // ===== 返回键 =====

    private val backPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (!back()) {
                    isEnabled = false
                    activityRef.get()?.onBackPressedDispatcher?.onBackPressed()
                    updateBackCallbackState()
                }
            }
        }

    // ===== 生命周期 =====

    private val lifecycleObserver =
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onHostDestroyed()
            }
        }

    private fun onHostDestroyed() {
        instances.remove(instanceKey)
        activityRef.get()?.let { removeActivityIndex(it, instanceKey) }
        unregisterHostCallbacks()
    }

    internal fun detachForReinit() {
        unregisterHostCallbacks()
    }

    private fun unregisterHostCallbacks() {
        hostFragmentRef?.get()?.let { host ->
            try { host.lifecycle.removeObserver(lifecycleObserver) } catch (_: Throwable) {}
            try { backPressedCallback.remove() } catch (_: Throwable) {}
            return
        }
        activityRef.get()?.let { a ->
            try { a.lifecycle.removeObserver(lifecycleObserver) } catch (_: Throwable) {}
            try { backPressedCallback.remove() } catch (_: Throwable) {}
        }
    }

    // ===== 公共属性 =====

    val currentRoute: String? get() = _currentRoute

    /** 当前 Tab ID */
    val currentTabId: String get() = activeTabId

    /** 当前活跃 Tab 的栈深度 */
    val stackDepth: Int get() = currentStack.size

    /** 供扩展使用。 */
    val hostFragmentManager: FragmentManager get() = fragmentManager

    /** 导航容器 id。 */
    val hostContainerId: Int get() = containerId

    // ===== 路由注册 =====

    fun register(route: String, cls: KClass<out Fragment>): AwNav {
        routes[route] = cls
        return this
    }

    inline fun <reified F : Fragment> register(route: String): AwNav = register(route, F::class)

    fun register(block: NavRouteBuilder.() -> Unit): AwNav {
        NavRouteBuilder(routes).apply(block)
        return this
    }

    fun addInterceptor(interceptor: NavInterceptor): AwNav {
        interceptors += interceptor
        return this
    }

    /**
     * 批量注册标注了 [AwNavRoute] 注解的 Fragment 类。
     */
    fun registerAnnotated(vararg classes: KClass<out Fragment>): AwNav {
        for (cls in classes) {
            val routeName =
                cls.java.getAnnotation(AwNavRoute::class.java)?.value
                    ?: throw IllegalArgumentException("${cls.simpleName} is not annotated with @AwNavRoute")
            if (routes.containsKey(routeName) && routes[routeName] != cls) {
                throw IllegalStateException(
                    "Route \"$routeName\" is already registered to ${routes[routeName]?.simpleName}, " +
                        "cannot re-register to ${cls.simpleName}",
                )
            }
            routes[routeName] = cls
        }
        return this
    }

    inline fun <reified F : Fragment> registerAnnotated(): AwNav = registerAnnotated(F::class)

    // ===== Tab 管理 =====

    /**
     * 初始化多 Tab。每个 [TabConfig] 定义一个 Tab 的 id 和根页面路由。
     *
     * 首个 Tab 的根页面会被自动导航。
     *
     * ```kotlin
     * nav.initTabs(
     *     TabConfig("home", rootRoute = "home"),
     *     TabConfig("contact", rootRoute = "contact"),
     * )
     * ```
     */
    @MainThread
    fun initTabs(vararg tabs: TabConfig) {
        assertMainThread("AwNav.initTabs")
        if (tabs.isEmpty()) return
        for (tab in tabs) {
            tabConfigs[tab.id] = tab
            if (!tabStacks.containsKey(tab.id)) {
                // 为每个 Tab 创建空栈，导航根页面
                val stack = mutableListOf<StackEntry>()
                tabStacks[tab.id] = stack
                navigateToTabRoot(tab.id, tab.rootRoute)
            }
        }
        // 激活第一个 Tab
        activeTabId = tabs.first().id
        attachTopOfTab(activeTabId)
        syncCurrentRoute()
    }

    /**
     * 切换 Tab。只做 `detach/attach`，Fragment 不会重建。
     */
    @MainThread
    fun switchTab(tabId: String) {
        assertMainThread("AwNav.switchTab")
        if (tabId == activeTabId) {
            onTabReselect?.invoke(tabId)
            return
        }
        if (!tabStacks.containsKey(tabId)) {
            throw IllegalArgumentException("Tab \"$tabId\" not found. Call initTabs() first.")
        }

        val animSet = tabConfigs[tabId]?.switchAnim?.resolve()

        fragmentManager.commit {
            setReorderingAllowed(true)
            animSet?.let { setCustomAnimations(it.enter, it.exit) }
            // detach 当前 Tab 所有 Fragment
            detachAllOfTabInTransaction(activeTabId)
            // attach 目标 Tab 栈顶
            attachTopOfTabInTransaction(tabId)
        }
        fragmentManager.executePendingTransactions()

        activeTabId = tabId
        syncCurrentRoute()
    }

    /**
     * 指定 Tab 的栈深度。
     */
    fun stackDepth(tabId: String): Int = tabStacks[tabId]?.size ?: 0

    /**
     * 是否可以返回（当前 Tab 栈深度 > 1）。
     */
    fun canGoBack(): Boolean = currentStack.size > 1

    // ===== 核心导航 =====

    /**
     * 导航到新页面。
     *
     * 新 Fragment 被 `add` 到容器并 `attach`/`show`，当前栈顶被 `detach`/`hide`。
     */
    @MainThread
    fun navigate(
        route: String,
        args: Bundle? = null,
        builder: (NavOptions.() -> Unit)? = null,
    ) {
        assertMainThread("AwNav.navigate")
        val cls =
            routes[route]
                ?: throw IllegalArgumentException(
                    "Route \"$route\" is not registered. Use register() or registerAnnotated() to register routes.",
                )

        if (fragmentManager.isStateSaved) {
            AwArch.logger.w("AwNav", "navigate(\"$route\") ignored: FragmentManager state already saved")
            return
        }

        val options = NavOptions().apply { builder?.invoke(this) }

        // 防连点
        if (!options.disableThrottle) {
            val now = SystemClock.uptimeMillis()
            val last = lastNavigateTimeByRoute.getOrPut(route) { AtomicLong(0L) }.get()
            if (now - last < NAV_THROTTLE_MILLIS) {
                if (AwArch.logAwNavThrottledNavigations) {
                    AwArch.logger.d("AwNav", "navigate(\"$route\") ignored: throttled")
                }
                return
            }
            lastNavigateTimeByRoute.getValue(route).set(now)
        }

        // 拦截器
        for (interceptor in interceptors) {
            if (!interceptor.onNavigate(_currentRoute, route, args)) return
        }

        val currentStack = currentStack

        // singleTop
        if (options.singleTop && _currentRoute == route) {
            val existing = currentStack.lastOrNull()?.fragment
            if (existing != null) {
                args?.let { existing.arguments = it }
                options.onSingleTopReuse?.invoke(existing)
                return
            }
        }

        // 创建 Fragment
        val fragment = fragmentManager.fragmentFactory.instantiate(
            cls.java.classLoader ?: ClassLoader.getSystemClassLoader(),
            cls.java.name,
        ).apply { arguments = args }

        // 获取 NavigatorTransaction
        val transaction = (fragment as? OnNavigatorTransactionListener)?.navigatorTransaction
            ?: NavigatorTransaction.ATTACH_DETACH

        val stackIndex = currentStack.size
        val tag = buildFragmentTag(activeTabId, route, stackIndex)

        // 执行 FragmentTransaction
        fragmentManager.commit {
            setReorderingAllowed(true)

            // 动画
            options.resolveAnimSet()?.let { a ->
                setCustomAnimations(a.enter, a.exit, a.popEnter, a.popExit)
            }

            // detach/hide 当前栈顶
            currentStack.lastOrNull()?.let { entry ->
                applyDetach(entry.fragment, entry.navigatorTransaction)
            }

            // add + attach/show 新 Fragment
            add(containerId, fragment, tag)
            applyAttach(fragment, transaction)
        }
        fragmentManager.executePendingTransactions()

        // 入栈
        val animSet = options.resolveAnimSet()
        currentStack.add(StackEntry(route, fragment, activeTabId, options.groupName, transaction, animSet))

        syncCurrentRoute()
    }

    /**
     * 返回上一页。返回 `true` 表示消费了返回事件。
     *
     * 会先检查栈顶 Fragment 是否实现了 [OnGoBackListener]。
     */
    @MainThread
    fun back(): Boolean {
        assertMainThread("AwNav.back")
        if (fragmentManager.isStateSaved) return false
        val stack = currentStack
        if (stack.size <= 1) return false

        // 检查 OnGoBackListener
        val topEntry = stack.last()
        if (topEntry.fragment is OnGoBackListener) {
            if (!(topEntry.fragment as OnGoBackListener).onGoBack()) {
                return true // Fragment 拦截了返回
            }
        }

        // 移除栈顶
        stack.removeAt(stack.lastIndex)

        fragmentManager.commit {
            setReorderingAllowed(true)
            // pop 动画：使用被移除页面记录的动画
            topEntry.animSet?.let { a ->
                setCustomAnimations(a.popEnter, a.popExit)
            }
            // 移除旧 Fragment
            remove(topEntry.fragment)
            // attach/show 新栈顶
            stack.lastOrNull()?.let { entry ->
                applyAttach(entry.fragment, entry.navigatorTransaction)
            }
        }
        fragmentManager.executePendingTransactions()

        syncCurrentRoute()
        return true
    }

    /**
     * 返回到指定路由（在当前 Tab 栈中查找）。
     *
     * @param inclusive 如果为 `true`，目标路由也会被移除
     * @return `true` 如果找到了目标路由并执行了弹出
     */
    @MainThread
    fun backTo(route: String, inclusive: Boolean = false): Boolean {
        assertMainThread("AwNav.backTo")
        if (fragmentManager.isStateSaved) return false
        val stack = currentStack
        val targetIndex = stack.indexOfLast { it.route == route }
        if (targetIndex < 0) return false

        val removeFrom = if (inclusive) targetIndex else targetIndex + 1
        val removeTo = stack.size
        if (removeFrom >= removeTo) return false

        // 要移除的条目
        val removed = stack.subList(removeFrom, removeTo).toList()
        // 新栈顶
        val newTopIndex = if (inclusive) targetIndex - 1 else targetIndex
        val newTop = if (newTopIndex >= 0) stack[newTopIndex] else null

        // 从栈中移除
        repeat(removed.size) { stack.removeAt(stack.lastIndex) }

        fragmentManager.commit {
            setReorderingAllowed(true)
            // pop 动画：使用栈顶被移除页面记录的动画
            removed.lastOrNull()?.animSet?.let { a ->
                setCustomAnimations(a.popEnter, a.popExit)
            }
            // 移除所有被弹出的 Fragment
            for (entry in removed) {
                remove(entry.fragment)
            }
            // attach/show 新栈顶
            newTop?.let { entry ->
                applyAttach(entry.fragment, entry.navigatorTransaction)
            }
        }
        fragmentManager.executePendingTransactions()

        syncCurrentRoute()
        return true
    }

    /**
     * 清空当前 Tab 栈并移除所有 Fragment。
     */
    @MainThread
    fun clearStack() {
        assertMainThread("AwNav.clearStack")
        if (fragmentManager.isStateSaved) return
        val stack = currentStack
        if (stack.isEmpty()) return

        fragmentManager.commit {
            setReorderingAllowed(true)
            for (entry in stack) {
                remove(entry.fragment)
            }
        }
        fragmentManager.executePendingTransactions()
        stack.clear()
        syncCurrentRoute()
    }

    /**
     * 清空当前 Tab 栈并导航到新页面。
     */
    @MainThread
    fun clearAndNavigate(
        route: String,
        args: Bundle? = null,
        builder: (NavOptions.() -> Unit)? = null,
    ) {
        assertMainThread("AwNav.clearAndNavigate")
        if (fragmentManager.isStateSaved) return
        clearStack()
        navigate(route, args, builder)
    }

    /**
     * 清除指定分组的所有页面。
     *
     * ```kotlin
     * nav.navigate("credit_card") { groupName = "PAYMENT" }
     * nav.navigate("address") { groupName = "PAYMENT" }
     * nav.navigate("success") { groupName = "PAYMENT" }
     * // 支付完成后一键清除
     * nav.clearGroup("PAYMENT")
     * ```
     */
    @MainThread
    fun clearGroup(groupName: String) {
        assertMainThread("AwNav.clearGroup")
        if (fragmentManager.isStateSaved) return
        val stack = currentStack
        val toRemove = stack.filter { it.groupName == groupName }
        if (toRemove.isEmpty()) return

        fragmentManager.commit {
            setReorderingAllowed(true)
            for (entry in toRemove) {
                remove(entry.fragment)
                stack.remove(entry)
            }
            // attach/show 当前栈顶
            stack.lastOrNull()?.let { entry ->
                applyAttach(entry.fragment, entry.navigatorTransaction)
            }
        }
        fragmentManager.executePendingTransactions()
        syncCurrentRoute()
    }

    /**
     * 导航到目标页面并监听返回结果。
     *
     * 基于 [FragmentManager.setFragmentResult] 实现，生命周期安全。
     */
    @MainThread
    fun navigateForResult(
        route: String,
        requestKey: String,
        args: Bundle? = null,
        builder: (NavOptions.() -> Unit)? = null,
        onResult: (Bundle) -> Unit,
    ) {
        assertMainThread("AwNav.navigateForResult")
        val lifecycleOwner = hostFragmentRef?.get()?.viewLifecycleOwner
            ?: activityRef.get()
            ?: return
        fragmentManager.setFragmentResultListener(requestKey, lifecycleOwner) { _, result ->
            onResult(result)
        }
        navigate(route, args, builder)
    }

    /**
     * 向 [navigateForResult] 的发起方回传结果。
     */
    @MainThread
    fun setFragmentResult(requestKey: String, result: Bundle) {
        fragmentManager.setFragmentResult(requestKey, result)
    }

    /**
     * 返回当前返回栈的结构化描述，用于调试。
     */
    fun dumpStack(): String {
        val sb = StringBuilder()
        for ((tabId, stack) in tabStacks) {
            val marker = if (tabId == activeTabId) " *" else ""
            sb.appendLine("Tab: $tabId (depth=${stack.size})$marker")
            for ((i, entry) in stack.withIndex()) {
                val groupInfo = entry.groupName?.let { " [group=$it]" } ?: ""
                val transInfo = entry.navigatorTransaction.name
                sb.appendLine("  [$i] ${entry.fragment::class.simpleName} (route=${entry.route}$groupInfo, $transInfo)")
            }
        }
        if (tabStacks.isEmpty()) {
            sb.appendLine("(no tabs initialized)")
        }
        sb.append("Current: ${_currentRoute ?: "-"} | Tab: $activeTabId | Stack depth: ${currentStack.size}")
        return sb.toString()
    }

    /**
     * 保存导航状态到 Bundle，用于进程重建恢复。
     *
     * 在 Activity/Fragment 的 `onSaveInstanceState` 中调用。
     */
    fun saveState(outState: Bundle) {
        outState.putString(KEY_ACTIVE_TAB, activeTabId)
    }

    // ===== 内部方法 =====

    private val currentStack: MutableList<StackEntry>
        get() = tabStacks.getOrPut(activeTabId) { mutableListOf() }

    // ===== Fragment Tag 管理 =====

    /**
     * 构建 Fragment tag，格式：`awnav|{tabId}|{route}|{stackIndex}`
     *
     * 确保 tag 全局唯一，支持同路由多 Tab 场景和进程重建恢复。
     */
    private fun buildFragmentTag(tabId: String, route: String, stackIndex: Int): String {
        return "awnav|$tabId|$route|$stackIndex"
    }

    /** 解析 Fragment tag，恢复栈信息 */
    private data class ParsedTag(
        val tabId: String,
        val route: String,
        val stackIndex: Int,
    )

    private fun parseFragmentTag(tag: String): ParsedTag? {
        if (!tag.startsWith(TAG_PREFIX)) return null
        val parts = tag.split(TAG_SEPARATOR)
        if (parts.size != 4) return null
        return try {
            ParsedTag(
                tabId = parts[1],
                route = parts[2],
                stackIndex = parts[3].toInt(),
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 从 FragmentManager 中恢复已存在的 Fragment（进程重建场景）。
     *
     * FM 会在进程重建后自动恢复 Fragment 实例（保留原始 tag），
     * 此方法解析 tag 重建 `tabStacks`。
     */
    private fun restoreFromFragmentManager(savedActiveTabId: String? = null) {
        val fragments = fragmentManager.fragments
        if (fragments.isEmpty()) return

        val restoredStacks = mutableMapOf<String, MutableList<Pair<Int, StackEntry>>>()

        for (fragment in fragments) {
            val tag = fragment.tag ?: continue
            val parsed = parseFragmentTag(tag) ?: continue

            val transaction = (fragment as? OnNavigatorTransactionListener)?.navigatorTransaction
                ?: NavigatorTransaction.ATTACH_DETACH

            val entry = StackEntry(
                route = parsed.route,
                fragment = fragment,
                tabId = parsed.tabId,
                navigatorTransaction = transaction,
            )

            val list = restoredStacks.getOrPut(parsed.tabId) { mutableListOf() }
            list.add(parsed.stackIndex to entry)
        }

        // 按 stackIndex 排序后重建 tabStacks
        for ((tabId, list) in restoredStacks) {
            list.sortBy { it.first }
            tabStacks[tabId] = list.map { it.second }.toMutableList()
        }

        // 恢复 activeTabId
        if (savedActiveTabId != null && tabStacks.containsKey(savedActiveTabId)) {
            activeTabId = savedActiveTabId
        } else if (tabStacks.isNotEmpty()) {
            activeTabId = tabStacks.keys.first()
        }

        // 确保只有活跃 Tab 的栈顶 Fragment 是 attach 的
        for ((tabId, stack) in tabStacks) {
            for (entry in stack) {
                if (tabId == activeTabId && entry === stack.last()) {
                    // 活跃 Tab 栈顶：确保 attach
                    if (entry.fragment.isDetached) {
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            applyAttach(entry.fragment, entry.navigatorTransaction)
                        }
                    }
                } else {
                    // 其他：确保 detach
                    if (!entry.fragment.isDetached && !entry.fragment.isHidden) {
                        fragmentManager.commit {
                            setReorderingAllowed(true)
                            applyDetach(entry.fragment, entry.navigatorTransaction)
                        }
                    }
                }
            }
        }
        fragmentManager.executePendingTransactions()

        syncCurrentRoute()
    }

    private fun navigateToTabRoot(tabId: String, rootRoute: String) {
        val cls = routes[rootRoute]
            ?: throw IllegalArgumentException("Root route \"$rootRoute\" for tab \"$tabId\" is not registered.")
        val stack = tabStacks.getOrPut(tabId) { mutableListOf() }
        if (stack.isNotEmpty()) return // 已有根页面

        val fragment = fragmentManager.fragmentFactory.instantiate(
            cls.java.classLoader ?: ClassLoader.getSystemClassLoader(),
            cls.java.name,
        )
        val transaction = (fragment as? OnNavigatorTransactionListener)?.navigatorTransaction
            ?: NavigatorTransaction.ATTACH_DETACH

        val tag = buildFragmentTag(tabId, rootRoute, 0)

        fragmentManager.commit {
            setReorderingAllowed(true)
            add(containerId, fragment, tag)
            // 根页面初始为 detached（等 switchTab 时 attach）
            applyDetach(fragment, transaction)
        }
        fragmentManager.executePendingTransactions()

        stack.add(StackEntry(rootRoute, fragment, tabId, navigatorTransaction = transaction))
    }

    private fun attachTopOfTab(tabId: String) {
        val stack = tabStacks[tabId] ?: return
        stack.lastOrNull()?.let { entry ->
            fragmentManager.commit {
                setReorderingAllowed(true)
                applyAttach(entry.fragment, entry.navigatorTransaction)
            }
            fragmentManager.executePendingTransactions()
        }
    }

    private fun detachAllOfTab(tabId: String) {
        val stack = tabStacks[tabId] ?: return
        fragmentManager.commit {
            setReorderingAllowed(true)
            for (entry in stack) {
                applyDetach(entry.fragment, entry.navigatorTransaction)
            }
        }
        fragmentManager.executePendingTransactions()
    }

    /** 在已有 FragmentTransaction 中 attach 目标 Tab 栈顶 */
    private fun FragmentTransaction.attachTopOfTabInTransaction(tabId: String) {
        val stack = tabStacks[tabId] ?: return
        stack.lastOrNull()?.let { entry ->
            applyAttach(entry.fragment, entry.navigatorTransaction)
        }
    }

    /** 在已有 FragmentTransaction 中 detach 当前 Tab 所有 Fragment */
    private fun FragmentTransaction.detachAllOfTabInTransaction(tabId: String) {
        val stack = tabStacks[tabId] ?: return
        for (entry in stack) {
            applyDetach(entry.fragment, entry.navigatorTransaction)
        }
    }

    /**
     * 将 Fragment 设为可见（attach 或 show）。
     * 必须在 FragmentTransaction 作用域内调用。
     */
    private fun FragmentTransaction.applyAttach(
        fragment: Fragment,
        transaction: NavigatorTransaction,
    ) {
        when (transaction) {
            NavigatorTransaction.SHOW_HIDE -> show(fragment)
            NavigatorTransaction.ATTACH_DETACH -> attach(fragment)
        }
    }

    /**
     * 将 Fragment 设为不可见（detach 或 hide）。
     * 必须在 FragmentTransaction 作用域内调用。
     */
    private fun FragmentTransaction.applyDetach(
        fragment: Fragment,
        transaction: NavigatorTransaction,
    ) {
        when (transaction) {
            NavigatorTransaction.SHOW_HIDE -> hide(fragment)
            NavigatorTransaction.ATTACH_DETACH -> detach(fragment)
        }
    }

    private fun syncCurrentRoute() {
        _currentRoute = currentStack.lastOrNull()?.route
        _routeFlow.value = _currentRoute
        updateBackCallbackState()
    }

    private fun updateBackCallbackState() {
        backPressedCallback.isEnabled = canGoBack()
    }

    private fun assertMainThread(method: String) {
        if (AwArch.strictMainThreadForAwNav && Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalStateException("$method must be called on the main thread")
        }
    }

    // ===== Companion =====

    companion object {
        private const val NAV_THROTTLE_MILLIS = 300L
        const val DEFAULT_TAB = "__default__"

        internal const val TAG_PREFIX = "awnav"
        internal const val TAG_SEPARATOR = "|"
        internal const val KEY_ACTIVE_TAB = "awnav_active_tab"

        private val instances = ConcurrentHashMap<Long, WeakReference<AwNav>>()
        private val activityIndex = ConcurrentHashMap<Int, MutableList<Long>>()
        private val nextKey = AtomicLong(0L)

        /**
         * 在 Activity 主容器上初始化导航。
         *
         * @param savedInstanceState Activity 的 savedInstanceState，用于进程重建恢复
         */
        fun init(
            activity: FragmentActivity,
            @IdRes containerId: Int,
            handleBackPressed: Boolean = true,
            savedInstanceState: Bundle? = null,
        ): AwNav {
            val key = nextKey.incrementAndGet()
            instances[key]?.get()?.detachForReinit()
            instances.remove(key)

            val nav = AwNav(
                WeakReference(activity),
                hostFragmentRef = null,
                activity.supportFragmentManager,
                containerId,
                key,
            )
            instances[key] = WeakReference(nav)
            addActivityIndex(activity, key)

            if (handleBackPressed) {
                activity.onBackPressedDispatcher.addCallback(activity, nav.backPressedCallback)
            }
            activity.lifecycle.addObserver(nav.lifecycleObserver)

            // 进程重建恢复
            val savedActiveTabId = savedInstanceState?.getString(KEY_ACTIVE_TAB)
            nav.restoreFromFragmentManager(savedActiveTabId)

            return nav
        }

        /**
         * 在 Fragment 子容器上初始化导航。
         *
         * @param savedInstanceState Fragment 的 savedInstanceState，用于进程重建恢复
         */
        fun init(
            host: Fragment,
            @IdRes containerId: Int,
            handleBackPressed: Boolean = true,
            savedInstanceState: Bundle? = null,
        ): AwNav {
            val key = nextKey.incrementAndGet()
            instances[key]?.get()?.detachForReinit()
            instances.remove(key)

            val activity = host.requireActivity()
            val nav = AwNav(
                WeakReference(activity),
                WeakReference(host),
                host.childFragmentManager,
                containerId,
                key,
            )
            instances[key] = WeakReference(nav)
            addActivityIndex(activity, key)

            if (handleBackPressed) {
                if (host.view == null) {
                    throw IllegalStateException(
                        "AwNav.init(host, ...) must be called after the host Fragment's view is created " +
                            "(e.g. in onViewCreated).",
                    )
                }
                host.requireActivity().onBackPressedDispatcher.addCallback(
                    host.viewLifecycleOwner,
                    nav.backPressedCallback,
                )
            }
            host.lifecycle.addObserver(nav.lifecycleObserver)

            // 进程重建恢复
            val savedActiveTabId = savedInstanceState?.getString(KEY_ACTIVE_TAB)
            nav.restoreFromFragmentManager(savedActiveTabId)

            return nav
        }

        /**
         * 从 Fragment 获取 AwNav：先查子容器 Host，再查 Activity 级实例。
         */
        fun from(fragment: Fragment): AwNav {
            val activity = fragment.requireActivity()
            val activityKeys = activityIndex[System.identityHashCode(activity)]
            if (activityKeys != null) {
                for (key in activityKeys) {
                    val nav = instances[key]?.get() ?: continue
                    if (nav.hostFragmentRef?.get() === fragment) return nav
                }
                var parent = fragment.parentFragment
                while (parent != null) {
                    for (key in activityKeys) {
                        val nav = instances[key]?.get() ?: continue
                        if (nav.hostFragmentRef?.get() === parent) return nav
                    }
                    parent = parent.parentFragment
                }
            }
            return from(activity)
        }

        fun from(activity: FragmentActivity): AwNav {
            val activityKeys = activityIndex[System.identityHashCode(activity)]
            if (activityKeys != null) {
                for (key in activityKeys) {
                    val nav = instances[key]?.get() ?: continue
                    if (nav.activityRef.get() === activity && nav.hostFragmentRef == null) return nav
                }
            }
            error(
                "AwNav not initialized for ${activity::class.simpleName}. " +
                    "Call AwNav.init(activity, containerId) or AwNav.init(hostFragment, containerId) first.",
            )
        }

        private fun addActivityIndex(activity: FragmentActivity, key: Long) {
            val id = System.identityHashCode(activity)
            activityIndex.getOrPut(id) { mutableListOf() }.add(key)
        }

        private fun removeActivityIndex(activity: FragmentActivity, key: Long) {
            val id = System.identityHashCode(activity)
            activityIndex[id]?.remove(key)
        }
    }
}

// ===== NavOptions =====

@AwArchDsl
class NavOptions {
    /** 当前路由与目标相同时是否复用实例。 */
    var singleTop: Boolean = false

    /** 为 `true` 时跳过 300ms 的 per-route 防连点节流。 */
    var disableThrottle: Boolean = false

    /** 当 [singleTop] 命中时触发，参数为复用的 Fragment 实例。 */
    var onSingleTopReuse: ((Fragment) -> Unit)? = null

    /** 内置转场动画，默认左右滑动。 */
    var anim: NavAnim = NavAnim.SLIDE_HORIZONTAL

    /** 分组名称，用于 [AwNav.clearGroup]。 */
    var groupName: String? = null

    @AnimRes private var customEnter: Int = 0
    @AnimRes private var customExit: Int = 0
    @AnimRes private var customPopEnter: Int = 0
    @AnimRes private var customPopExit: Int = 0

    fun setCustomAnim(
        @AnimRes enter: Int,
        @AnimRes exit: Int,
        @AnimRes popEnter: Int = 0,
        @AnimRes popExit: Int = 0,
    ) {
        anim = NavAnim.NONE
        customEnter = enter
        customExit = exit
        customPopEnter = popEnter
        customPopExit = popExit
    }

    internal fun resolveAnimSet(): AnimSet? {
        if (anim != NavAnim.NONE) return anim.resolve()
        if (customEnter != 0 || customExit != 0) {
            return AnimSet(customEnter, customExit, customPopEnter, customPopExit)
        }
        return null
    }
}

// ===== NavAnim =====

enum class NavAnim {
    NONE,
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    ;

    internal fun resolve(): AnimSet? =
        when (this) {
            NONE -> null
            FADE -> AnimSet(
                R.anim.aw_nav_fade_in,
                R.anim.aw_nav_fade_out,
                R.anim.aw_nav_fade_in,
                R.anim.aw_nav_fade_out,
            )
            SLIDE_HORIZONTAL -> AnimSet(
                R.anim.aw_nav_slide_in_right,
                R.anim.aw_nav_slide_out_left,
                R.anim.aw_nav_slide_in_left,
                R.anim.aw_nav_slide_out_right,
            )
            SLIDE_VERTICAL -> AnimSet(
                R.anim.aw_nav_slide_in_up,
                R.anim.aw_nav_fade_out,
                R.anim.aw_nav_fade_in,
                R.anim.aw_nav_slide_out_down,
            )
        }
}

// ===== AnimSet =====

data class AnimSet(
    @AnimRes val enter: Int,
    @AnimRes val exit: Int,
    @AnimRes val popEnter: Int,
    @AnimRes val popExit: Int,
)

// ===== NavInterceptor =====

fun interface NavInterceptor {
    fun onNavigate(from: String?, to: String, args: Bundle?): Boolean
}

// ===== NavRouteBuilder =====

@AwArchDsl
class NavRouteBuilder internal constructor(
    private val routes: MutableMap<String, KClass<out Fragment>>,
) {
    inline fun <reified F : Fragment> route(name: String) {
        addRoute(name, F::class)
    }

    @PublishedApi
    internal fun addRoute(name: String, cls: KClass<out Fragment>) {
        routes[name] = cls
    }
}
