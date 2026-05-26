package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.View
import com.answufeng.arch.demo.R
import com.answufeng.arch.nav.AwNavHostFragment

/**
 * 盖在 Tab + BottomNav 之上的全屏层；内层页面由 [AwNav]（child 容器）管理。
 */
class WeChatOverlayHostFragment : AwNavHostFragment(R.layout.fragment_overlay_host, R.id.inner_container) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nav.register {
            route<ChatDetailFragment>("chat_detail")
            route<ChatInfoFragment>("chat_info")
            route<ContactDetailFragment>("contact_detail")
            route<ContactExtraFragment>("contact_extra")
        }
        childFragmentManager.addOnBackStackChangedListener {
            (activity as? WeChatActivity)?.syncWeChatChrome()
        }
    }

    fun navigate(route: String, args: android.os.Bundle? = null) {
        nav.navigate(route, args)
    }

    fun popInner(): Boolean = nav.back()

    val innerStackDepth: Int
        get() = nav.stackDepth
}
