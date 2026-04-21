package com.answufeng.arch.mvvm

import android.os.Bundle
import com.answufeng.arch.base.MvvmViewModel.UiEvent

/**
 * MVVM 视图接口，定义 [UiEvent] 的默认处理逻辑。
 *
 * 所有 MVVM 基类（Activity/Fragment/DialogFragment/BottomSheetDialogFragment）
 * 均实现此接口，统一处理 ViewModel 发出的 UI 事件。
 *
 * 子类可覆写 [showToast]、[onLoading]、[navigateTo]、[navigateBack]、[handleCustomEvent]
 * 来自定义事件处理行为。
 *
 * @see MvvmViewModel.UiEvent
 */
interface MvvmView {
    /**
     * 处理 ViewModel 发出的 [UiEvent]，默认分发到各具体方法。
     *
     * @param event 由 ViewModel 发出的一次性事件
     */
    fun onUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Toast -> showToast(event.message)
            is UiEvent.Loading -> onLoading(event.show)
            is UiEvent.Navigate -> navigateTo(event.route, event.extras)
            is UiEvent.NavigateBack -> navigateBack()
            is UiEvent.Custom -> handleCustomEvent(event.key, event.data)
        }
    }

    /**
     * Loading 状态变化回调。
     *
     * @param show `true` 表示显示 Loading，`false` 表示隐藏
     */
    fun onLoading(show: Boolean) {}

    /**
     * 显示 Toast 消息。
     *
     * @param message 要显示的消息文本
     */
    fun showToast(message: String) {}

    /**
     * 导航到指定路由。
     *
     * @param route 目标路由路径
     * @param extras 导航参数，可为 null
     */
    fun navigateTo(route: String, extras: Bundle? = null) {}

    /**
     * 返回上一页。
     */
    fun navigateBack() {}

    /**
     * 处理自定义事件。
     *
     * @param key 事件标识
     * @param data 事件携带的数据
     */
    fun handleCustomEvent(key: String, data: Any?) {}
}
