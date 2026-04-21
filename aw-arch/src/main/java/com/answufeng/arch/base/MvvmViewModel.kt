package com.answufeng.arch.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVVM 模式 ViewModel 基类，通过 [UiEvent] 通道向 UI 层发送一次性事件。
 *
 * 内置常用事件：[UiEvent.Toast]、[UiEvent.Loading]、[UiEvent.Navigate]、[UiEvent.NavigateBack]。
 * 自定义事件可使用 [UiEvent.Custom] 或继承 [UiEvent]。
 *
 * ```kotlin
 * class MyViewModel : MvvmViewModel() {
 *     fun loadData() = launchIO {
 *         showLoading()
 *         val data = repository.fetch()
 *         showLoading(false)
 *         navigate("/detail", Bundle().apply { putInt("id", data.id) })
 *     }
 * }
 * ```
 */
open class MvvmViewModel(
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val uiEventChannel = Channel<UiEvent>(Channel.UNLIMITED)

    /** UI 事件流，消费后不会重放 */
    val uiEvent: Flow<UiEvent> = uiEventChannel.receiveAsFlow()

    /** 兼容旧 API，建议改用 [uiEvent] */
    @Deprecated("Use uiEvent instead", ReplaceWith("uiEvent"))
    val UiEvent: Flow<UiEvent> = uiEvent

    /** 发送自定义 UiEvent */
    protected open fun sendEvent(event: UiEvent) {
        uiEventChannel.trySend(event)
    }

    /** 发送 Toast 事件 */
    protected open fun showToast(message: String) = sendEvent(UiEvent.Toast(message))

    /** 发送 Loading 显示/隐藏事件 */
    protected open fun showLoading(show: Boolean = true) = sendEvent(UiEvent.Loading(show))

    /** 发送导航事件，[extras] 使用 Bundle 传递参数 */
    protected open fun navigate(route: String, extras: Bundle? = null) =
        sendEvent(UiEvent.Navigate(route, extras))

    /** 发送返回事件 */
    protected open fun navigateBack() = sendEvent(UiEvent.NavigateBack)

    /**
     * UI 事件密封类，定义 ViewModel 向 View 发送的一次性事件。
     *
     * - [Toast]: 显示 Toast 消息
     * - [Loading]: 显示/隐藏加载状态
     * - [Navigate]: 导航到指定路由
     * - [NavigateBack]: 返回上一页
     * - [Custom]: 自定义事件，通过 key-value 传递数据
     */
    sealed class UiEvent {
        data class Toast(val message: String) : UiEvent()
        data class Loading(val show: Boolean) : UiEvent()
        data class Navigate(val route: String, val extras: Bundle? = null) : UiEvent()
        data object NavigateBack : UiEvent()
        data class Custom(val key: String, val data: Any? = null) : UiEvent()
    }
}
