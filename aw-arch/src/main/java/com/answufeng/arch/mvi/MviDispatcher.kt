package com.answufeng.arch.mvi

import androidx.annotation.MainThread

/**
 * MVI 意图分发器接口，提供直接分发和节流分发能力。
 *
 * 通常由 ViewModel 实现，Fragment/Activity 通过此接口发送用户意图。
 *
 * ```kotlin
 * // 在 Fragment / Activity（实现 MviDispatcher）中
 * dispatch(MyIntent.Refresh)
 * dispatchThrottled(MyIntent.Submit) // 300ms 内只处理一次（默认同类 Intent 共用一个节流 key）
 * ```
 *
 * @param I 意图类型，必须继承 [UiIntent]
 */
interface MviDispatcher<I : UiIntent> {

    /**
     * 分发意图，立即处理。
     *
     * 必须在主线程调用。
     *
     * @param intent 用户意图
     */
    @MainThread
    fun dispatch(intent: I)

    /**
     * 节流分发意图，在 [windowMillis] 时间窗口内相同 key 的意图只处理第一次。
     *
     * 适用于按钮防抖、快速重复点击等场景。必须在主线程调用。
     *
     * @param intent       用户意图
     * @param windowMillis 时间窗口（毫秒），默认 300
     * @param keySelector  意图去重 key 选择器，默认使用意图类名
     */
    @MainThread
    fun dispatchThrottled(intent: I, windowMillis: Long = 300, keySelector: (I) -> String = { it::class.java.name })
}
