package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiIntent

sealed class WeChatIntent : UiIntent {
    data class SwitchTab(val tabIndex: Int) : WeChatIntent()
}
