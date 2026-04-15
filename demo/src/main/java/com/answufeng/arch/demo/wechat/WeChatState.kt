package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiState

data class WeChatState(
    val currentTab: Int = 0,
    val unreadCount: Map<String, Int> = emptyMap()
) : UiState
