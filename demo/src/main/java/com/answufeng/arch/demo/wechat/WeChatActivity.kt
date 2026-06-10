package com.answufeng.arch.demo.wechat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.ActivityWechatBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.BackDispatcherChain
import com.answufeng.arch.nav.NavAnim
import com.answufeng.arch.nav.TabConfig
import kotlinx.coroutines.launch

/**
 * 演示两种层级：
 * - **Tab 内（AwNav）**：`container` 内切换微信/通讯录/发现/我，每个 Tab Fragment 自带标题栏。
 * - **全屏 overlay**：内层由 [WeChatOverlayHostFragment] 的 **AwNav.init(host)** 管理子栈，
 *   每个 overlay Fragment 自带 toolbar（含返回按钮），整页动画。
 */
class WeChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWechatBinding
    private lateinit var nav: AwNav
    private lateinit var backChain: BackDispatcherChain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWechatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nav =
            AwNav.init(this, R.id.container, handleBackPressed = false, savedInstanceState = savedInstanceState)
                .register<WeChatFragment>("wechat")
                .register<ContactFragment>("contact")
                .register<DiscoverFragment>("discover")
                .register<ProfileFragment>("profile")

        nav.initTabs(
            TabConfig("wechat", rootRoute = "wechat", switchAnim = NavAnim.FADE),
            TabConfig("contact", rootRoute = "contact", switchAnim = NavAnim.FADE),
            TabConfig("discover", rootRoute = "discover", switchAnim = NavAnim.FADE),
            TabConfig("profile", rootRoute = "profile", switchAnim = NavAnim.FADE),
        )

        backChain =
            BackDispatcherChain(onBackPressedDispatcher, this)
                .add(100) { popOverlayOrDismiss() }
                .add(50) {
                    if (nav.canGoBack()) nav.back() else false
                }
                .install(enabled = false)

        // 监听路由变化，更新返回链启用状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                nav.currentRouteFlow.collect {
                    backChain.setEnabled(binding.overlay.isVisible || nav.canGoBack())
                }
            }
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            val initialTab = intent.getStringExtra(EXTRA_INITIAL_TAB)
            val tabId =
                when (initialTab) {
                    TAB_CONTACT -> "contact"
                    TAB_DISCOVER -> "discover"
                    TAB_PROFILE -> "profile"
                    else -> "wechat"
                }
            binding.bottomNavigation.selectedItemId =
                when (tabId) {
                    "contact" -> R.id.tab_contact
                    "discover" -> R.id.tab_discover
                    "profile" -> R.id.tab_profile
                    else -> R.id.tab_wechat
                }
            nav.switchTab(tabId)

            when (intent.getStringExtra(EXTRA_OPEN_OVERLAY)) {
                OVERLAY_CHAT_DETAIL -> pushOverlayRoute("chat_detail")
                OVERLAY_CHAT_INFO -> {
                    pushOverlayRoute("chat_detail")
                    pushOverlayRoute("chat_info", bundleOf("title" to "Chat A"))
                }
                OVERLAY_CONTACT_DETAIL -> pushOverlayRoute("contact_detail", bundleOf("name" to "小明"))
                OVERLAY_CONTACT_EXTRA -> {
                    pushOverlayRoute("contact_detail", bundleOf("name" to "小明"))
                    pushOverlayRoute("contact_extra", bundleOf("name" to "小明"))
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nav.saveState(outState)
    }

    fun updateBackChainState() {
        backChain.setEnabled(binding.overlay.isVisible || nav.canGoBack())
    }

    fun pushOverlayRoute(route: String, args: Bundle? = null) {
        val isNewOverlay = overlayHost() == null
        binding.overlay.isVisible = true
        val host = ensureOverlayHost()
        host.navigate(route, args)
        backChain.setEnabled(true)
        if (isNewOverlay) {
            binding.overlay.translationX = binding.overlay.width.toFloat()
            binding.overlay.animate()
                .translationX(0f)
                .setDuration(250)
                .start()
        }
    }

    private fun ensureOverlayHost(): WeChatOverlayHostFragment {
        var host = overlayHost()
        if (host == null) {
            host = WeChatOverlayHostFragment()
            supportFragmentManager.commit {
                replace(R.id.overlay, host, "overlay_host")
            }
            supportFragmentManager.executePendingTransactions()
        }
        return host
    }

    private fun overlayHost(): WeChatOverlayHostFragment? =
        supportFragmentManager.findFragmentById(R.id.overlay) as? WeChatOverlayHostFragment

    private fun dismissOverlayCompletely() {
        binding.overlay.animate()
            .translationX(binding.overlay.width.toFloat())
            .setDuration(250)
            .withEndAction {
                binding.overlay.isVisible = false
                binding.overlay.translationX = 0f
                overlayHost()?.let { host ->
                    supportFragmentManager.commit { remove(host) }
                }
                backChain.setEnabled(nav.canGoBack())
            }
            .start()
    }

    fun popOverlayOrDismiss(): Boolean {
        val host = overlayHost() ?: return false
        if (!binding.overlay.isVisible) return false
        // overlay 内层栈 > 1 时，先 pop 内层
        if (host.innerStackDepth > 1) {
            host.popInner()
            return true
        }
        // 内层只剩根页面或为空，直接 dismiss overlay
        dismissOverlayCompletely()
        return true
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            dismissOverlayCompletely()
            val tabId =
                when (item.itemId) {
                    R.id.tab_wechat -> "wechat"
                    R.id.tab_contact -> "contact"
                    R.id.tab_discover -> "discover"
                    R.id.tab_profile -> "profile"
                    else -> return@setOnItemSelectedListener false
                }
            nav.switchTab(tabId)
            true
        }
    }

    companion object {
        const val EXTRA_INITIAL_TAB: String = "aw_demo_wechat_initial_tab"
        const val EXTRA_OPEN_OVERLAY: String = "aw_demo_wechat_open_overlay"

        const val TAB_CONTACT: String = "contact"
        const val TAB_DISCOVER: String = "discover"
        const val TAB_PROFILE: String = "profile"

        const val OVERLAY_CHAT_DETAIL: String = "overlay_chat_detail"
        const val OVERLAY_CHAT_INFO: String = "overlay_chat_info"
        const val OVERLAY_CONTACT_DETAIL: String = "overlay_contact_detail"
        const val OVERLAY_CONTACT_EXTRA: String = "overlay_contact_extra"
    }
}
