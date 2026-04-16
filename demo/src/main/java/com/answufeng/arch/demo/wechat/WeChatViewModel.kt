package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.MviViewModel

class WeChatViewModel : MviViewModel<WeChatState, WeChatEvent, WeChatIntent>(WeChatState()) {

    override fun handleIntent(intent: WeChatIntent) {
        when (intent) {
            is WeChatIntent.SwitchTab -> {
                updateState { copy(currentTab = intent.tabIndex) }
                sendMviEvent(WeChatEvent.TabChanged(intent.tabIndex))
            }
        }
    }

    fun updateUnreadCount(tab: String, count: Int) {
        updateState {
            copy(unreadCount = unreadCount + (tab to count))
        }
    }
}
