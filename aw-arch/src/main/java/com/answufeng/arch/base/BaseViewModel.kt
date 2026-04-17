package com.answufeng.arch.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.answufeng.arch.config.AwArch
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
        AwArch.logger.e("BaseViewModel", "Unhandled exception in ${this::class.simpleName}", throwable)
    }
}
