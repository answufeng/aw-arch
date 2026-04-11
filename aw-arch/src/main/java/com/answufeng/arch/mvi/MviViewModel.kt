package com.answufeng.arch.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.answufeng.arch.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

abstract class MviViewModel<S : UiState, E : UiEvent, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val _state = MutableStateFlow(initialState)

    val state: StateFlow<S> = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    private val _event = Channel<E>(Channel.BUFFERED)

    val event = _event.receiveAsFlow()

    abstract fun handleIntent(intent: I)

    fun dispatch(intent: I) {
        handleIntent(intent)
    }

    private val intentThrottleMap = ConcurrentHashMap<String, Long>()

    fun dispatchThrottled(intent: I, windowMillis: Long = 300) {
        val key = intent::class.java.name
        val now = System.nanoTime() / 1_000_000L
        val last = intentThrottleMap[key] ?: 0L
        if (now - last >= windowMillis) {
            intentThrottleMap[key] = now
            handleIntent(intent)
        }
    }

    protected fun updateState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    protected fun sendMviEvent(event: E) {
        viewModelScope.launch { _event.send(event) }
    }

    override fun handleException(throwable: Throwable) {
        com.answufeng.arch.config.BrickArch.logger.e("MviViewModel", "Unhandled exception in ${this::class.simpleName}", throwable)
    }
}
