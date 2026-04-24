package com.answufeng.arch.demo.wechat

import android.content.Intent
import android.os.Bundle
import com.answufeng.arch.demo.BaseMenuActivity

class WeChatMenuActivity : BaseMenuActivity() {
    override val pageTitle: String = "AwNav · 微信式多 Tab"
    override val pageDesc: String =
        "这个示例比较复杂：Tab 内用 AwNav，外层 Overlay 用独立 host + child backstack。\n" +
            "下面按“入口场景”拆成多个启动项。"

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("默认启动（微信 Tab）", intent = Intent(this, WeChatActivity::class.java))

        addMenuItem(
            "启动到 通讯录 Tab",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_INITIAL_TAB, WeChatActivity.TAB_CONTACT),
        )
        addMenuItem(
            "启动到 发现 Tab",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_INITIAL_TAB, WeChatActivity.TAB_DISCOVER),
        )
        addMenuItem(
            "启动到 我 Tab",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_INITIAL_TAB, WeChatActivity.TAB_PROFILE),
        )

        addMenuItem(
            "直接打开 Overlay：会话详情（第 2 层）",
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_OPEN_OVERLAY, WeChatActivity.OVERLAY_CHAT_DETAIL),
        )
        addMenuItem(
            "直接打开 Overlay：聊天资料（第 3 层）",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_OPEN_OVERLAY, WeChatActivity.OVERLAY_CHAT_INFO),
        )
        addMenuItem(
            "直接打开 Overlay：联系人详情（第 2 层）",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_OPEN_OVERLAY, WeChatActivity.OVERLAY_CONTACT_DETAIL),
        )
        addMenuItem(
            "直接打开 Overlay：联系人更多（第 3 层）",
            outlined = true,
            intent = Intent(this, WeChatActivity::class.java).putExtra(WeChatActivity.EXTRA_OPEN_OVERLAY, WeChatActivity.OVERLAY_CONTACT_EXTRA),
        )
    }
}

