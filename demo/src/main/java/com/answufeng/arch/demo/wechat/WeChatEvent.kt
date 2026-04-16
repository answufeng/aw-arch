package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiEvent

sealed class WeChatEvent : UiEvent {
    data class TabChanged(val tabIndex: Int) : WeChatEvent()
}
