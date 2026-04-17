package com.answufeng.arch.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class MvvmViewModel(
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val _uiEvent = Channel<UIEvent>(Channel.UNLIMITED)

    val uiEvent: Flow<UIEvent> = _uiEvent.receiveAsFlow()

    protected open fun sendEvent(event: UIEvent) {
        _uiEvent.trySend(event)
    }

    protected open fun showToast(message: String) = sendEvent(UIEvent.Toast(message))

    protected open fun showLoading(show: Boolean = true) = sendEvent(UIEvent.Loading(show))

    protected open fun navigate(route: String, extras: Map<String, Any>? = null) =
        sendEvent(UIEvent.Navigate(route, extras))

    protected open fun navigateBack() = sendEvent(UIEvent.NavigateBack)

    sealed class UIEvent {
        data class Toast(val message: String) : UIEvent()
        data class Loading(val show: Boolean) : UIEvent()
        data class Navigate(val route: String, val extras: Map<String, Any>? = null) : UIEvent()
        data object NavigateBack : UIEvent()
        data class Custom(val key: String, val data: Any? = null) : UIEvent()
    }
}
