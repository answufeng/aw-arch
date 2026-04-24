package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle

class AwNavMenuActivity : BaseMenuActivity() {
    override val pageTitle: String = "AwNav"
    override val pageDesc: String = "路由基础 / 拦截器演示分开页面；微信式多 Tab 保持独立示例。"

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("基础路由（Home / Detail / Settings / Back）", intent = Intent(this, AwNavBasicRouteDemoActivity::class.java))
        addMenuItem("拦截器（拦截 detail）", outlined = true, intent = Intent(this, AwNavInterceptorDemoActivity::class.java))
        addMenuItem("微信式多 Tab（复杂示例）", outlined = true, intent = Intent(this, com.answufeng.arch.demo.wechat.WeChatMenuActivity::class.java))
    }
}

