package com.answufeng.arch.mvi

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import com.answufeng.arch.base.BaseViewModel
import com.answufeng.arch.config.AwArch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

abstract class MviViewModel<S : UiState, E : UiEvent, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val _state = MutableStateFlow(initialState)

    val state: StateFlow<S> = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    private val _event = Channel<E>(Channel.UNLIMITED)

    val event: Flow<E> = _event.receiveAsFlow()

    private val intentThrottleMap = ConcurrentHashMap<String, Long>()

    abstract fun handleIntent(intent: I)

    fun dispatch(intent: I) {
        handleIntent(intent)
    }

    fun dispatchThrottled(intent: I, windowMillis: Long = 300) {
        val key = intent::class.java.name
        val now = SystemClock.elapsedRealtime()
        val last = intentThrottleMap[key] ?: 0L
        if (now - last >= windowMillis) {
            intentThrottleMap[key] = now
            handleIntent(intent)
        }
    }

    protected fun clearThrottleCache() {
        intentThrottleMap.clear()
    }

    protected fun updateState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    protected fun sendMviEvent(event: E) {
        _event.trySend(event)
    }

    override fun onCleared() {
        super.onCleared()
        intentThrottleMap.clear()
    }

    override fun handleException(throwable: Throwable) {
        AwArch.logger.e("MviViewModel", "Unhandled exception in ${this::class.simpleName}", throwable)
    }
}
