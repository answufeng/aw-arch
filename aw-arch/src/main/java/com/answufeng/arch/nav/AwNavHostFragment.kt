package com.answufeng.arch.nav

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

/**
 * 可选的 AwNav 宿主 Fragment：在 [onViewCreated] 中对 [navContainerId] 调用 [AwNav.init]。
 *
 * ```xml
 * <!-- fragment_my_host.xml -->
 * <FrameLayout
 *     android:id="@+id/nav_host_container"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * ```kotlin
 * class MyHostFragment : AwNavHostFragment(R.layout.fragment_my_host, R.id.nav_host_container) {
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         nav.register<MyDetailFragment>("detail")
 *         if (savedInstanceState == null) {
 *             nav.navigate("detail")
 *         }
 *     }
 * }
 * ```
 */
abstract class AwNavHostFragment(
    layoutResId: Int,
    @IdRes private val navContainerId: Int,
) : Fragment(layoutResId) {

    /** 子类可重写为 `false` 以禁用 AwNav 内置的返回键处理（例如由外部 BackDispatcherChain 统一管理）。 */
    protected open val handleBackPressed: Boolean = true

    protected lateinit var nav: AwNav
        private set

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        nav = AwNav.init(this, navContainerId, handleBackPressed)
    }
}
