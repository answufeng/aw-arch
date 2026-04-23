package com.answufeng.arch.mvvm

import com.answufeng.arch.base.MvvmViewModel.UiEvent
import com.answufeng.arch.nav.AwNav

/**
 * 将 [UiEvent] 分发给 [AwNav]（若提供），否则走 [MvvmView.onUiEvent]。
 *
 * - [UiEvent.Navigate] → [AwNav.navigate]
 * - [UiEvent.NavigateBack] → [AwNav.back]；若返回栈为空则调用 [onBackWhenNavEmpty]（默认与 [MvvmView.navigateBack] 一致）
 *
 * 在 Activity / Fragment 基类中覆写 [com.answufeng.arch.mvvm.MvvmActivity.awNav] 等方式提供 [AwNav] 实例。
 */
fun MvvmView.dispatchMvvmUiEvent(
    event: UiEvent,
    awNav: AwNav?,
    onBackWhenNavEmpty: () -> Unit,
) {
    if (awNav != null) {
        when (event) {
            is UiEvent.Navigate -> awNav.navigate(event.route, event.extras)
            is UiEvent.NavigateBack -> {
                if (!awNav.back()) onBackWhenNavEmpty()
            }
            else -> onUiEvent(event)
        }
    } else {
        onUiEvent(event)
    }
}
