package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiIntent

sealed class WeChatIntent : UiIntent {
    data object LoadMessages : WeChatIntent()
    data class SwitchTab(val index: Int) : WeChatIntent()
}
