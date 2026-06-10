package com.answufeng.arch.nav

/**
 * 标注在 Fragment 类上，声明其默认路由名。
 *
 * 使用此注解后，无需在 [AwNav.init] 时手动调用 [AwNav.register]，
 * [AwNav.navigate] 遇到未注册的路由时会自动查找标注了此注解的 Fragment。
 *
 * ```kotlin
 * @AwNavRoute("home")
 * class HomeFragment : BaseFragment<FragmentNavHomeBinding>() { ... }
 *
 * // 无需 register，直接 navigate
 * nav.navigate("home")
 * ```
 *
 * **注意**：同一路由名只能标注一个 Fragment 类，否则运行时会抛出异常。
 * 如果多个 Tab 使用同一个 Fragment 类，请用 [AwNavTab.fragmentCls] 代替注解方式。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AwNavRoute(val value: String)
