package com.answufeng.arch.mvi

interface MviDispatcher<I : UiIntent> {
    fun dispatch(intent: I)
    fun dispatchThrottled(intent: I, windowMillis: Long = 300, keySelector: (I) -> String = { it::class.java.name })
}
