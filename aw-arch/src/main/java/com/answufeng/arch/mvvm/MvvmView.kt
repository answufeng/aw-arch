package com.answufeng.arch.mvvm

import android.os.Bundle
import com.answufeng.arch.base.MvvmViewModel.UIEvent

/**
 * MVVM 视图接口，定义 [UIEvent] 的默认处理逻辑。
 *
 * 所有 MVVM 基类（Activity/Fragment/DialogFragment/BottomSheetDialogFragment）
 * 均实现此接口，统一处理 ViewModel 发出的 UI 事件。
 *
 * 子类可覆写 [showToast]、[onLoading]、[navigateTo]、[navigateBack]、[handleCustomEvent]
 * 来自定义事件处理行为。
 */
interface MvvmView {
    /** 处理 ViewModel 发出的 UIEvent，默认分发到各具体方法 */
    fun onUIEvent(event: UIEvent) {
        when (event) {
            is UIEvent.Toast -> showToast(event.message)
            is UIEvent.Loading -> onLoading(event.show)
            is UIEvent.Navigate -> navigateTo(event.route, event.extras)
            is UIEvent.NavigateBack -> navigateBack()
            is UIEvent.Custom -> handleCustomEvent(event.key, event.data)
        }
    }

    /** Loading 状态变化回调 */
    fun onLoading(show: Boolean) {}
    /** 显示 Toast，各基类提供默认实现 */
    fun showToast(message: String) {}
    /** 导航到指定路由 */
    fun navigateTo(route: String, extras: Bundle? = null) {}
    /** 返回上一页 */
    fun navigateBack() {}
    /** 处理自定义事件 */
    fun handleCustomEvent(key: String, data: Any?) {}
}
