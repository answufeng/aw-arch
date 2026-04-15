package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiEvent

data class TabChangedEvent(val tabIndex: Int) : UiEvent
