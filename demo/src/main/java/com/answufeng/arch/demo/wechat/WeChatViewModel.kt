package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.MviViewModel

class WeChatViewModel : MviViewModel<WeChatState, WeChatEvent, WeChatIntent>(WeChatState()) {

    override fun handleIntent(intent: WeChatIntent) {
        when (intent) {
            is SwitchTabIntent -> {
                updateState { copy(currentTab = intent.tabIndex) }
                sendMviEvent(TabChangedEvent(intent.tabIndex))
            }
        }
    }

    fun updateUnreadCount(tab: String, count: Int) {
        updateState {
            copy(unreadCount = unreadCount + (tab to count))
        }
    }
}
