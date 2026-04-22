package com.answufeng.arch.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.answufeng.arch.config.AwArch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
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
 *
 * [UiEvent] 通道为有界队列（默认 [UI_EVENT_CHANNEL_CAPACITY]）；满时按 [BufferOverflow.DROP_OLDEST] 丢弃最旧未消费事件，避免无界堆积。
 */
open class MvvmViewModel(
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val uiEventChannel = Channel<UiEvent>(
        capacity = UI_EVENT_CHANNEL_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** UI 事件流，消费后不会重放 */
    val uiEvent: Flow<UiEvent> = uiEventChannel.receiveAsFlow()

    /** 发送自定义 UiEvent */
    protected open fun sendEvent(event: UiEvent) {
        val result = uiEventChannel.trySend(event)
        if (!result.isSuccess) {
            AwArch.logger.w(
                "MvvmViewModel",
                "UiEvent not delivered (channel closed or failed): ${event::class.simpleName}"
            )
        }
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

    override fun onCleared() {
        uiEventChannel.close()
        super.onCleared()
    }

    companion object {
        const val UI_EVENT_CHANNEL_CAPACITY: Int = 128
    }
}
