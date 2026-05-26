package com.answufeng.arch.nav

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner

/**
 * 按优先级串联多个返回处理逻辑（数值越大越先执行）。
 *
 * ```kotlin
 * BackDispatcherChain(onBackPressedDispatcher, this)
 *     .add(100) { popOverlay() }
 *     .add(50) { nav.back() }
 *     .install()
 * ```
 *
 * 全部未处理时，会临时禁用自身并委托系统 [OnBackPressedDispatcher.onBackPressed]。
 */
class BackDispatcherChain(
    private val dispatcher: OnBackPressedDispatcher,
    private val owner: LifecycleOwner,
) {
    private val handlers = mutableListOf<Pair<Int, () -> Boolean>>()
    private var callback: OnBackPressedCallback? = null

    /**
     * @param handler 返回 `true` 表示已消费返回事件
     * @return this，便于链式调用
     */
    fun add(
        priority: Int,
        handler: () -> Boolean,
    ): BackDispatcherChain {
        handlers.add(priority to handler)
        handlers.sortByDescending { it.first }
        return this
    }

    @MainThread
    fun install(enabled: Boolean = true): BackDispatcherChain {
        callback?.remove()
        callback =
            object : OnBackPressedCallback(enabled) {
                override fun handleOnBackPressed() {
                    for ((_, handler) in handlers) {
                        if (handler()) return
                    }
                    isEnabled = false
                    dispatcher.onBackPressed()
                    isEnabled = true
                }
            }.also {
                dispatcher.addCallback(owner, it)
            }
        return this
    }

    fun setEnabled(enabled: Boolean) {
        callback?.isEnabled = enabled
    }

    fun remove() {
        callback?.remove()
        callback = null
    }
}
