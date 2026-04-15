package com.answufeng.arch.demo.wechat

import com.answufeng.arch.mvi.UiIntent

data class SwitchTabIntent(val tabIndex: Int) : UiIntent
