package com.answufeng.arch.demo.hub

import android.content.Intent
import android.os.Bundle
import com.answufeng.arch.demo.AwNavBasicRouteDemoActivity
import com.answufeng.arch.demo.AwNavInterceptorDemoActivity
import com.answufeng.arch.demo.AwNavTabStackDemoActivity
import com.answufeng.arch.demo.BaseMenuActivity
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.wechat.WeChatActivity

class NavMenuActivity : BaseMenuActivity() {
    override val pageTitle: String get() = getString(R.string.demo_hub_nav_title)
    override val pageDesc: String get() = getString(R.string.demo_hub_nav_desc)

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem(
            "基础路由",
            subtitle = "navigate · back · backTo · clearStack · singleTop · currentRouteFlow",
            intent = Intent(this, AwNavBasicRouteDemoActivity::class.java),
        )
        addMenuItem(
            "路由拦截器",
            subtitle = "addInterceptor · 登录拦截模拟",
            outlined = true,
            intent = Intent(this, AwNavInterceptorDemoActivity::class.java),
        )
        addMenuItem(
            "Tab 独立返回栈",
            subtitle = "AwNavTabSwitcher · onReselect · switchAnim",
            outlined = true,
            intent = Intent(this, AwNavTabStackDemoActivity::class.java),
        )
        addMenuItem(
            "多 Tab + Overlay",
            subtitle = "Tab 栈 · 全屏层 · 内层 AwNav · BackDispatcherChain",
            intent = Intent(this, WeChatActivity::class.java),
        )
    }
}
