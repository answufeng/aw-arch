package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.MviViewModel

class WeChatViewModel : MviViewModel<WeChatState, WeChatEvent, WeChatIntent>(WeChatState()) {

    override fun handleIntent(intent: WeChatIntent) {
        when (intent) {
            WeChatIntent.LoadMessages -> loadMessages()
            is WeChatIntent.SwitchTab -> updateState { copy(currentTab = intent.index) }
        }
    }

    private fun loadMessages() = launchIO {
        updateState { copy(isLoading = true) }
        kotlinx.coroutines.delay(800)
        val messages = listOf(
            Message("张三", "你好，最近怎么样？", "10:30"),
            Message("李四", "周末一起吃饭吗？", "09:15"),
            Message("王五", "项目进展如何？", "昨天"),
            Message("赵六", "收到，谢谢！", "昨天"),
            Message("孙七", "明天开会别忘了", "周一")
        )
        updateState { copy(isLoading = false, messages = messages) }
        sendMviEvent(WeChatEvent.ShowMessage("消息加载完成"))
    }
}
