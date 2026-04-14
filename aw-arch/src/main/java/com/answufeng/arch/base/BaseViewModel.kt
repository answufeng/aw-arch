package com.answufeng.arch.base

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answufeng.arch.config.BrickArch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    protected val savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    private fun resolveHandler(onError: ((Throwable) -> Unit)?): CoroutineExceptionHandler {
        return if (onError != null) {
            CoroutineExceptionHandler { _, throwable -> onError(throwable) }
        } else {
            exceptionHandler
        }
    }

    protected fun launch(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(resolveHandler(onError), block = block)
    }

    protected fun launchIO(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO + resolveHandler(onError), block = block)
    }

    protected fun launchDefault(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Default + resolveHandler(onError), block = block)
    }

    protected suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Main, block)

    protected inline fun <reified T> getSavedState(key: String): T? =
        savedStateHandle?.get<T>(key)

    protected fun <T> setSavedState(key: String, value: T) {
        savedStateHandle?.set(key, value)
    }

    protected fun <T> savedStateFlow(key: String, initialValue: T): kotlinx.coroutines.flow.StateFlow<T> =
        savedStateHandle?.getStateFlow(key, initialValue)
            ?: kotlinx.coroutines.flow.MutableStateFlow(initialValue)

    protected open fun handleException(throwable: Throwable) {
        BrickArch.logger.e("BaseViewModel", "Unhandled exception in ${this::class.simpleName}", throwable)
    }
}

abstract class MvvmViewModel(
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

    @Immutable
    sealed class UIEvent {
        data class Toast(val message: String) : UIEvent()
        data class Loading(val show: Boolean) : UIEvent()
        data class Navigate(val route: String, val extras: Map<String, Any>? = null) : UIEvent()
        data object NavigateBack : UIEvent()
        data class Custom(val key: String, val data: Any? = null) : UIEvent()
    }
}
