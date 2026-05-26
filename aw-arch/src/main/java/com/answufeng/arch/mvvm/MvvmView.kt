package com.answufeng.arch.mvvm

import com.answufeng.arch.base.ArchView
import com.answufeng.arch.base.MvvmViewModel.UiEvent

/**
 * MVVM 视图接口，定义 [UiEvent] 的默认处理逻辑。
 *
 * 所有 MVVM 基类（Activity/Fragment/DialogFragment/BottomSheetDialogFragment）
 * 均实现此接口，统一处理 ViewModel 发出的 UI 事件。
 * 通用 UI 能力（Toast / Loading / 导航）见 [ArchView]。
 *
 * 子类可覆写 [ArchView.showToast]、[ArchView.onLoading]、[ArchView.navigateTo]、[ArchView.navigateBack]、[handleCustomEvent]
 * 来自定义事件处理行为。
 *
 * @see MvvmViewModel.UiEvent
 * @see ArchView
 */
interface MvvmView : ArchView {
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
     * 处理自定义事件。
     *
     * @param key 事件标识
     * @param data 事件携带的数据
     */
    fun handleCustomEvent(
        key: String,
        data: Any?,
    ) {}
}
