package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.MviEffect

sealed class WeChatEvent : MviEffect {
    data class ShowMessage(val message: String) : WeChatEvent()
}
