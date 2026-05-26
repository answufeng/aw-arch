package com.answufeng.arch.demo.wechat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import com.answufeng.arch.R as ArchR
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.ActivityWechatBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.AwNavTab
import com.answufeng.arch.nav.BackDispatcherChain
/**
 * 演示两种层级：
 * - **Tab 内（AwNav）**：`container` 内切换微信/通讯录/发现/我。
 * - **全屏 overlay**：内层由 [WeChatOverlayHostFragment] 的 **AwNav.init(host)** 管理子栈。
 */
class WeChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWechatBinding
    private lateinit var nav: AwNav
    private lateinit var tabSwitcher: com.answufeng.arch.nav.AwNavTabSwitcher
    private lateinit var backChain: BackDispatcherChain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWechatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        nav =
            AwNav.init(this, R.id.container, handleBackPressed = false)
                .register<WeChatFragment>("wechat")
                .register<ContactFragment>("contact")
                .register<DiscoverFragment>("discover")
                .register<ProfileFragment>("profile")

        tabSwitcher =
            nav.tabSwitcher(
                listOf(
                    AwNavTab(id = "wechat", rootRoute = "wechat"),
                    AwNavTab(id = "contact", rootRoute = "contact"),
                    AwNavTab(id = "discover", rootRoute = "discover"),
                    AwNavTab(id = "profile", rootRoute = "profile"),
                ),
            )

        backChain =
            BackDispatcherChain(onBackPressedDispatcher, this)
                .add(100) { popOverlayOrDismiss() }
                .add(50) { nav.back() }
                .install(enabled = false)

        supportFragmentManager.addOnBackStackChangedListener { syncWeChatChrome() }

        binding.toolbar.setNavigationOnClickListener {
            popOverlayOrDismiss()
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
            tabSwitcher.selectTab(tabId)

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
        } else {
            tabSwitcher.restoreState(savedInstanceState)
            syncWeChatChrome()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::tabSwitcher.isInitialized) {
            tabSwitcher.saveState(outState)
        }
    }

    fun syncWeChatChrome() {
        val host = overlayHost()
        val showing = host != null && binding.overlay.isVisible
        binding.toolbar.navigationIcon = if (showing) {
            AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back_24)
        } else {
            null
        }
        binding.toolbar.subtitle = if (showing && host != null) {
            getString(R.string.wechat_overlay_stack, host.innerStackDepth)
        } else {
            null
        }
        backChain.setEnabled(showing || nav.stackDepth > 0)
    }

    fun pushOverlayRoute(route: String, args: Bundle? = null) {
        binding.overlay.isVisible = true
        val host = ensureOverlayHost()
        host.navigate(route, args)
        syncWeChatChrome()
    }

    private fun ensureOverlayHost(): WeChatOverlayHostFragment {
        var host = overlayHost()
        if (host == null) {
            host = WeChatOverlayHostFragment()
            supportFragmentManager.commit {
                setCustomAnimations(
                    ArchR.anim.aw_nav_slide_in_right,
                    ArchR.anim.aw_nav_slide_out_left,
                    ArchR.anim.aw_nav_slide_in_left,
                    ArchR.anim.aw_nav_slide_out_right,
                )
                replace(R.id.overlay, host, "overlay_host")
            }
            supportFragmentManager.executePendingTransactions()
        }
        return host
    }

    private fun overlayHost(): WeChatOverlayHostFragment? =
        supportFragmentManager.findFragmentById(R.id.overlay) as? WeChatOverlayHostFragment

    private fun dismissOverlayCompletely() {
        overlayHost()?.let { host ->
            supportFragmentManager.commit {
                remove(host)
            }
        }
        binding.overlay.isVisible = false
        syncWeChatChrome()
    }

    private fun popOverlayOrDismiss(): Boolean {
        val host = overlayHost() ?: return false
        if (!binding.overlay.isVisible) return false
        if (host.innerStackDepth > 0) {
            if (host.popInner()) {
                if (host.innerStackDepth == 0) {
                    dismissOverlayCompletely()
                } else {
                    syncWeChatChrome()
                }
            }
            return true
        }
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
            tabSwitcher.selectTab(tabId)
            syncWeChatChrome()
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
