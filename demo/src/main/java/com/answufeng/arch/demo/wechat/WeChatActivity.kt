package com.answufeng.arch.demo.wechat

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.answufeng.arch.R as ArchR
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.ActivityWechatBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.NavAnim

/**
 * 演示两种层级：
 * - **Tab 内（AwNav）**：`container` 内切换微信/通讯录/发现/我，与 [BottomNavigationView] 同属一层。
 * - **全屏外层**：`overlay` 盖住 Tab+底栏（底栏不必 [View.GONE]，避免整页高度抽动）；内层再跳转走 [WeChatOverlayHostFragment] 的 **child** 栈，与 AwNav 的 FM 返回栈隔离。
 */
class WeChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWechatBinding
    private lateinit var nav: AwNav

    private val overlayBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            popOverlayOrDismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWechatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, overlayBackCallback)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        nav = AwNav.init(this, R.id.container)
            .register<WeChatFragment>("wechat")
            .register<ContactFragment>("contact")
            .register<DiscoverFragment>("discover")
            .register<ProfileFragment>("profile")

        supportFragmentManager.addOnBackStackChangedListener { syncWeChatChrome() }

        binding.toolbar.setNavigationOnClickListener {
            popOverlayOrDismiss()
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.tab_wechat
        } else {
            syncWeChatChrome()
        }
    }

    /** 供 [WeChatOverlayHostFragment] 在内层栈变化时刷新 Toolbar */
    fun syncWeChatChrome() {
        val host = supportFragmentManager.findFragmentById(R.id.overlay) as? WeChatOverlayHostFragment
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
        overlayBackCallback.isEnabled = showing
    }

    /**
     * 打开/叠加大屏流程中的一页（第 2、3 层均走内层 child 栈）。
     */
    fun pushOverlayPage(fragment: Fragment, tag: String) {
        binding.overlay.isVisible = true
        var host = supportFragmentManager.findFragmentById(R.id.overlay) as? WeChatOverlayHostFragment
        if (host == null) {
            host = WeChatOverlayHostFragment()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    ArchR.anim.aw_nav_slide_in_right,
                    ArchR.anim.aw_nav_slide_out_left,
                    ArchR.anim.aw_nav_slide_in_left,
                    ArchR.anim.aw_nav_slide_out_right,
                )
                .replace(R.id.overlay, host, "overlay_host")
                .commit()
            supportFragmentManager.executePendingTransactions()
        }
        host.pushPage(fragment, tag)
        syncWeChatChrome()
    }

    private fun dismissOverlayCompletely() {
        val host = supportFragmentManager.findFragmentById(R.id.overlay)
        if (host != null) {
            supportFragmentManager.beginTransaction()
                .remove(host)
                .commit()
        }
        binding.overlay.isVisible = false
        syncWeChatChrome()
    }

    /**
     * @return true 已消费（内层 pop 或整层关闭）
     */
    private fun popOverlayOrDismiss(): Boolean {
        val host = supportFragmentManager.findFragmentById(R.id.overlay) as? WeChatOverlayHostFragment
            ?: return false
        if (!binding.overlay.isVisible) return false
        if (host.innerStackDepth > 0) {
            host.childFragmentManager.popBackStackImmediate()
            if (host.innerStackDepth == 0) {
                dismissOverlayCompletely()
            } else {
                syncWeChatChrome()
            }
            return true
        }
        dismissOverlayCompletely()
        return true
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            dismissOverlayCompletely()
            nav.clearStack()
            when (item.itemId) {
                R.id.tab_wechat -> nav.navigate("wechat") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_contact -> nav.navigate("contact") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_discover -> nav.navigate("discover") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_profile -> nav.navigate("profile") { addToBackStack = false; anim = NavAnim.NONE }
            }
            syncWeChatChrome()
            true
        }
    }
}
