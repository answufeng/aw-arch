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
import androidx.fragment.app.commit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.answufeng.arch.R
import com.answufeng.arch.config.AwArch
import com.answufeng.arch.config.AwArchDsl
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

/**
 * 轻量级 Fragment 导航控制器，替代 Navigation Component 的简化方案。
 *
 * 功能：
 * - 路由注册与 Fragment 实例化
 * - 拦截器（如登录拦截）
 * - 返回栈管理
 * - DSL 批量注册
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
    private val instanceKey: Int,
) {
    private val routes = mutableMapOf<String, KClass<out Fragment>>()
    private val interceptors = mutableListOf<NavInterceptor>()

    private var _currentRoute: String? = null

    private val lastNavigateTimeByRoute = ConcurrentHashMap<String, AtomicLong>()

    private val backPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (!back()) {
                    isEnabled = false
                    activityRef.get()?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }

    private val fragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(
                fm: FragmentManager,
                f: Fragment,
            ) {
                syncCurrentRoute()
            }
        }

    private val lifecycleObserver =
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onHostDestroyed()
            }
        }

    private fun onHostDestroyed() {
        instances.remove(instanceKey)
        unregisterHostCallbacks()
    }

    internal fun detachForReinit() {
        unregisterHostCallbacks()
    }

    private fun unregisterHostCallbacks() {
        hostFragmentRef?.get()?.let { host ->
            try {
                host.lifecycle.removeObserver(lifecycleObserver)
            } catch (_: Throwable) {
            }
            try {
                host.childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
            } catch (_: Throwable) {
            }
            try {
                backPressedCallback.remove()
            } catch (_: Throwable) {
            }
            return
        }
        activityRef.get()?.let { a ->
            try {
                a.lifecycle.removeObserver(lifecycleObserver)
            } catch (_: Throwable) {
            }
            try {
                a.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
            } catch (_: Throwable) {
            }
            try {
                backPressedCallback.remove()
            } catch (_: Throwable) {
            }
        }
    }

    val currentRoute: String? get() = _currentRoute

    val stackDepth: Int get() = fragmentManager.backStackEntryCount

    /** 供 [AwNavTabSwitcher] 等扩展使用。 */
    val hostFragmentManager: FragmentManager get() = fragmentManager

    /** 导航容器 id（[AwNav.init] 传入的 `containerId`）。 */
    val hostContainerId: Int
        get() = containerId

    fun register(
        route: String,
        cls: KClass<out Fragment>,
    ): AwNav {
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

    private fun assertMainThread(method: String) {
        if (AwArch.strictMainThreadForAwNav && Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalStateException("$method must be called on the main thread")
        }
    }

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
                    "Route \"$route\" is not registered. Available routes: ${routes.keys}",
                )

        if (fragmentManager.isStateSaved) {
            AwArch.logger.w("AwNav", "navigate(\"$route\") ignored: FragmentManager state already saved")
            return
        }

        val options = NavOptions().apply { builder?.invoke(this) }

        if (!options.disableThrottle) {
            val now = SystemClock.uptimeMillis()
            val last =
                lastNavigateTimeByRoute
                    .getOrPut(route) { AtomicLong(0L) }
                    .get()
            if (now - last < NAV_THROTTLE_MILLIS) {
                if (AwArch.logAwNavThrottledNavigations) {
                    AwArch.logger.d(
                        "AwNav",
                        "navigate(\"$route\") ignored: throttled (${NAV_THROTTLE_MILLIS}ms per-route window)",
                    )
                }
                return
            }
            lastNavigateTimeByRoute.getValue(route).set(now)
        }

        for (interceptor in interceptors) {
            if (!interceptor.onNavigate(_currentRoute, route, args)) return
        }

        if (options.singleTop && _currentRoute == route) {
            val existing = fragmentManager.findFragmentByTag(route)
            if (existing != null) {
                if (args != null) {
                    existing.arguments = args
                }
                return
            }
        }

        val fragment =
            fragmentManager.fragmentFactory.instantiate(
                cls.java.classLoader ?: ClassLoader.getSystemClassLoader(),
                cls.java.name,
            ).apply {
                arguments = args
            }

        fragmentManager.commit {
            setReorderingAllowed(true)
            options.resolveAnimSet()?.let { a ->
                setCustomAnimations(a.enter, a.exit, a.popEnter, a.popExit)
            }
            replace(containerId, fragment, route)
            if (options.addToBackStack) {
                addToBackStack(options.backStackName ?: route)
            }
        }

        _currentRoute = route
        updateBackCallbackState()
    }

    @MainThread
    fun back(): Boolean {
        assertMainThread("AwNav.back")
        if (fragmentManager.isStateSaved) return false
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
            syncCurrentRoute()
            return true
        }
        return false
    }

    @MainThread
    fun backTo(
        route: String,
        inclusive: Boolean = false,
    ): Boolean {
        assertMainThread("AwNav.backTo")
        if (fragmentManager.isStateSaved) return false
        val flag = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        val result = fragmentManager.popBackStackImmediate(route, flag)
        syncCurrentRoute()
        return result
    }

    @MainThread
    fun clearStack() {
        assertMainThread("AwNav.clearStack")
        if (fragmentManager.isStateSaved) return
        if (fragmentManager.backStackEntryCount > 0) {
            val first = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStackImmediate(
                first.id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE,
            )
        }
        val containerFragment = fragmentManager.findFragmentById(containerId)
        if (containerFragment != null) {
            fragmentManager.commit {
                setReorderingAllowed(true)
                remove(containerFragment)
            }
        }
        _currentRoute = null
        syncCurrentRoute()
    }

    internal fun syncCurrentRoute() {
        val count = fragmentManager.backStackEntryCount
        _currentRoute =
            if (count > 0) {
                fragmentManager.getBackStackEntryAt(count - 1).name
            } else {
                fragmentManager.findFragmentById(containerId)?.tag
            }
        updateBackCallbackState()
    }

    private fun updateBackCallbackState() {
        backPressedCallback.isEnabled = fragmentManager.backStackEntryCount > 0
    }

    /**
     * BottomNav 等多 Tab 场景：各 Tab 独立返回栈（[FragmentManager.saveBackStack] / [restoreBackStack]）。
     */
    fun tabSwitcher(tabs: List<AwNavTab>): AwNavTabSwitcher =
        AwNavTabSwitcher(
            nav = this,
            fragmentManager = fragmentManager,
            containerId = containerId,
            tabs = tabs,
        )

    companion object {
        private const val NAV_THROTTLE_MILLIS = 300L

        private val instances = ConcurrentHashMap<Int, WeakReference<AwNav>>()

        /**
         * 在 Activity 主容器上初始化导航（绑定 [FragmentActivity.supportFragmentManager]）。
         */
        fun init(
            activity: FragmentActivity,
            @IdRes containerId: Int,
            handleBackPressed: Boolean = true,
        ): AwNav {
            val key = instanceKeyFor(activity)
            instances[key]?.get()?.detachForReinit()
            instances.remove(key)

            val nav =
                AwNav(
                    WeakReference(activity),
                    hostFragmentRef = null,
                    activity.supportFragmentManager,
                    containerId,
                    key,
                )
            instances[key] = WeakReference(nav)

            if (handleBackPressed) {
                activity.onBackPressedDispatcher.addCallback(activity, nav.backPressedCallback)
            }
            nav.syncCurrentRoute()
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                nav.fragmentLifecycleCallbacks,
                false,
            )
            activity.lifecycle.addObserver(nav.lifecycleObserver)
            return nav
        }

        /**
         * 在 Fragment 子容器上初始化导航（绑定 [Fragment.childFragmentManager]）。
         *
         * 适用于 Tab 内多级页面、overlay Host 内层栈等场景。
         * [from] 会优先解析当前 Fragment 及其 [Fragment.getParentFragment] 链上的实例，再回退到 Activity 级 AwNav。
         */
        fun init(
            host: Fragment,
            @IdRes containerId: Int,
            handleBackPressed: Boolean = true,
        ): AwNav {
            val key = instanceKeyFor(host)
            instances[key]?.get()?.detachForReinit()
            instances.remove(key)

            val activity = host.requireActivity()
            val nav =
                AwNav(
                    WeakReference(activity),
                    WeakReference(host),
                    host.childFragmentManager,
                    containerId,
                    key,
                )
            instances[key] = WeakReference(nav)

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
            nav.syncCurrentRoute()
            host.childFragmentManager.registerFragmentLifecycleCallbacks(
                nav.fragmentLifecycleCallbacks,
                false,
            )
            host.lifecycle.addObserver(nav.lifecycleObserver)
            return nav
        }

        /**
         * 从 Fragment 获取 AwNav：先查子容器 Host，再查 Activity 级实例。
         */
        fun from(fragment: Fragment): AwNav {
            var host: Fragment? = fragment
            while (host != null) {
                instances[instanceKeyFor(host)]?.get()?.let { return it }
                host = host.parentFragment
            }
            return from(fragment.requireActivity())
        }

        fun from(activity: FragmentActivity): AwNav {
            val key = instanceKeyFor(activity)
            return instances[key]?.get()
                ?: error(
                    "AwNav not initialized for ${activity::class.simpleName}. " +
                        "Call AwNav.init(activity, containerId) or AwNav.init(hostFragment, containerId) first.",
                )
        }

        private fun instanceKeyFor(activity: FragmentActivity): Int = System.identityHashCode(activity)

        private fun instanceKeyFor(host: Fragment): Int = System.identityHashCode(host)
    }
}

@AwArchDsl
class NavOptions {
    /** 是否将本次导航加入 Fragment 返回栈，默认 `true`。 */
    var addToBackStack: Boolean = true

    /**
     * 自定义入栈名称；默认与 route 相同。
     * Tab 根页面请设为 [AwNavTab.backStackName]，以便 [AwNavTabSwitcher] 保存/恢复各 Tab 栈。
     */
    var backStackName: String? = null

    /** 当前路由与 [route] 相同时是否复用实例；为 `true` 时会更新 [args]（若提供）。 */
    var singleTop: Boolean = false

    /** 为 `true` 时跳过 300ms 的 per-route 防连点节流。 */
    var disableThrottle: Boolean = false

    /** 内置转场动画，默认左右滑动。 */
    var anim: NavAnim = NavAnim.SLIDE_HORIZONTAL

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

enum class NavAnim {
    NONE,
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    ;

    internal fun resolve(): AnimSet? =
        when (this) {
            NONE -> null
            FADE ->
                AnimSet(
                    R.anim.aw_nav_fade_in,
                    R.anim.aw_nav_fade_out,
                    R.anim.aw_nav_fade_in,
                    R.anim.aw_nav_fade_out,
                )
            SLIDE_HORIZONTAL ->
                AnimSet(
                    R.anim.aw_nav_slide_in_right,
                    R.anim.aw_nav_slide_out_left,
                    R.anim.aw_nav_slide_in_left,
                    R.anim.aw_nav_slide_out_right,
                )
            SLIDE_VERTICAL ->
                AnimSet(
                    R.anim.aw_nav_slide_in_up,
                    R.anim.aw_nav_fade_out,
                    R.anim.aw_nav_fade_in,
                    R.anim.aw_nav_slide_out_down,
                )
        }
}

internal data class AnimSet(
    @AnimRes val enter: Int,
    @AnimRes val exit: Int,
    @AnimRes val popEnter: Int,
    @AnimRes val popExit: Int,
)

fun interface NavInterceptor {
    fun onNavigate(
        from: String?,
        to: String,
        args: Bundle?,
    ): Boolean
}

@AwArchDsl
class NavRouteBuilder internal constructor(
    private val routes: MutableMap<String, KClass<out Fragment>>,
) {
    inline fun <reified F : Fragment> route(name: String) {
        addRoute(name, F::class)
    }

    @PublishedApi
    internal fun addRoute(
        name: String,
        cls: KClass<out Fragment>,
    ) {
        routes[name] = cls
    }
}
