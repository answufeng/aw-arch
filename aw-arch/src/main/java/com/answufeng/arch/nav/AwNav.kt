package com.answufeng.arch.nav

import android.os.Bundle
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
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import com.answufeng.arch.config.AwArchDsl
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
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
) {

    private val routes = mutableMapOf<String, KClass<out Fragment>>()
    private val interceptors = mutableListOf<NavInterceptor>()

    private var _currentRoute: String? = null

    private var lastNavigateTime = 0L

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (!back()) {
                isEnabled = false
                activityRef.get()?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            syncCurrentRoute()
        }
    }

    val currentRoute: String? get() = _currentRoute

    val stackDepth: Int get() = fragmentManager.backStackEntryCount

    fun register(route: String, cls: KClass<out Fragment>): AwNav {
        routes[route] = cls
        return this
    }

    inline fun <reified F : Fragment> register(route: String): AwNav =
        register(route, F::class)

    fun register(block: NavRouteBuilder.() -> Unit): AwNav {
        NavRouteBuilder(routes).apply(block)
        return this
    }

    fun addInterceptor(interceptor: NavInterceptor): AwNav {
        interceptors += interceptor
        return this
    }

    @MainThread
    fun navigate(
        route: String,
        args: Bundle? = null,
        builder: (NavOptions.() -> Unit)? = null,
    ) {
        val cls = routes[route]
            ?: throw IllegalArgumentException(
                "Route \"$route\" is not registered. Available routes: ${routes.keys}"
            )

        if (fragmentManager.isStateSaved) return

        val now = SystemClock.uptimeMillis()
        if (now - lastNavigateTime < NAV_THROTTLE_MILLIS) return
        lastNavigateTime = now

        val options = NavOptions().apply { builder?.invoke(this) }

        for (interceptor in interceptors) {
            if (!interceptor.onNavigate(_currentRoute, route, args)) return
        }

        if (options.singleTop && _currentRoute == route) return

        val fragment = fragmentManager.fragmentFactory.instantiate(
            cls.java.classLoader ?: ClassLoader.getSystemClassLoader(),
            cls.java.name,
        ).apply {
            arguments = args
        }

        fragmentManager.commit {
            options.resolveAnimSet()?.let { a ->
                setCustomAnimations(a.enter, a.exit, a.popEnter, a.popExit)
            }
            replace(containerId, fragment, route)
            if (options.addToBackStack) {
                addToBackStack(route)
            }
        }

        _currentRoute = route
        updateBackCallbackState()
    }

    @MainThread
    fun back(): Boolean {
        if (fragmentManager.isStateSaved) return false
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
            syncCurrentRoute()
            return true
        }
        return false
    }

    @MainThread
    fun backTo(route: String, inclusive: Boolean = false): Boolean {
        if (fragmentManager.isStateSaved) return false
        val flag = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        val result = fragmentManager.popBackStackImmediate(route, flag)
        syncCurrentRoute()
        return result
    }

    @MainThread
    fun clearStack() {
        if (fragmentManager.isStateSaved) return
        if (fragmentManager.backStackEntryCount > 0) {
            val first = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStackImmediate(
                first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE,
            )
            syncCurrentRoute()
        }
    }

    private fun syncCurrentRoute() {
        val count = fragmentManager.backStackEntryCount
        _currentRoute = if (count > 0) {
            fragmentManager.getBackStackEntryAt(count - 1).name
        } else {
            fragmentManager.findFragmentById(containerId)?.tag
        }
        updateBackCallbackState()
    }

    private fun updateBackCallbackState() {
        backPressedCallback.isEnabled = fragmentManager.backStackEntryCount > 0
    }

    companion object {
        private const val NAV_THROTTLE_MILLIS = 300L

        private val instances = ConcurrentHashMap<Int, WeakReference<AwNav>>()

        fun init(activity: FragmentActivity, @IdRes containerId: Int): AwNav {
            val key = System.identityHashCode(activity)
            val nav = AwNav(WeakReference(activity), activity.supportFragmentManager, containerId)
            instances[key] = WeakReference(nav)

            activity.onBackPressedDispatcher.addCallback(activity, nav.backPressedCallback)

            nav.syncCurrentRoute()

            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(nav.fragmentLifecycleCallbacks, false)

            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    instances.remove(key)
                    activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(nav.fragmentLifecycleCallbacks)
                }
            })
            return nav
        }

        fun from(fragment: Fragment): AwNav {
            val key = System.identityHashCode(fragment.requireActivity())
            return instances[key]?.get()
                ?: error("AwNav not initialized for ${fragment.requireActivity()::class.simpleName}. Call AwNav.init() in your Activity's onCreate() first.")
        }

        fun from(activity: FragmentActivity): AwNav {
            val key = System.identityHashCode(activity)
            return instances[key]?.get()
                ?: error("AwNav not initialized for ${activity::class.simpleName}. Call AwNav.init() in your Activity's onCreate() first.")
        }
    }
}

@AwArchDsl
class NavOptions {
    var addToBackStack: Boolean = true
    var singleTop: Boolean = false
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
    SLIDE_VERTICAL;

    internal fun resolve(): AnimSet? = when (this) {
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

internal data class AnimSet(
    @AnimRes val enter: Int,
    @AnimRes val exit: Int,
    @AnimRes val popEnter: Int,
    @AnimRes val popExit: Int,
)

fun interface NavInterceptor {
    fun onNavigate(from: String?, to: String, args: Bundle?): Boolean
}

@AwArchDsl
class NavRouteBuilder internal constructor(
    private val routes: MutableMap<String, KClass<out Fragment>>
) {
    inline fun <reified F : Fragment> route(name: String) {
        addRoute(name, F::class)
    }

    @PublishedApi
    internal fun addRoute(name: String, cls: KClass<out Fragment>) {
        routes[name] = cls
    }
}
