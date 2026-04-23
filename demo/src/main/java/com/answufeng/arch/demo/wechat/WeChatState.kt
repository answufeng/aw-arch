package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiState

data class WeChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val currentTab: Int = 0
) : UiState

data class Message(
    val id: String,
    val sender: String,
    val content: String,
    val time: String = "",
)
