package com.answufeng.arch.ext

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.mvi.UiEvent
import com.answufeng.arch.mvi.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

fun <T : Any> Flow<T>.collectOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect { action(it) }
        }
    }
}

fun <T> Flow<T>.throttleFirst(windowMillis: Long): Flow<T> {
    return ThrottleFirstFlow(this, windowMillis)
}

internal class ThrottleFirstFlow<T>(
    private val source: Flow<T>,
    private val windowMillis: Long
) : Flow<T> by source {
    override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<T>) {
        var lastTime = 0L
        source.collect { value ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastTime >= windowMillis) {
                lastTime = now
                collector.emit(value)
            }
        }
    }
}

fun <T> Flow<T>.debounceAction(timeoutMillis: Long): Flow<T> {
    return debounce(timeoutMillis)
}

fun <T, R> StateFlow<T>.select(selector: (T) -> R): Flow<R> {
    return map(selector).distinctUntilChanged()
}

fun <T, R> Flow<T>.select(selector: (T) -> R): Flow<R> {
    return map(selector).distinctUntilChanged()
}

fun View.throttleClicks(windowMillis: Long = 300): Flow<Unit> {
    val channel = Channel<Unit>(Channel.CONFLATED)
    setOnClickListener {
        channel.trySend(Unit)
    }
    return channel.receiveAsFlow()
}

fun <S : UiState, E : UiEvent> LifecycleOwner.observeMvi(
    stateFlow: StateFlow<S>,
    eventFlow: Flow<E>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    render: (S) -> Unit,
    handleEvent: (E) -> Unit = {}
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            launch { stateFlow.collect { render(it) } }
            launch { eventFlow.collect { handleEvent(it) } }
        }
    }
}
