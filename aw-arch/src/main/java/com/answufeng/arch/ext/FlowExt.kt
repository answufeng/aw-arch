package com.answufeng.arch.ext

import android.view.View
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun <T> Flow<T>.throttleFirst(windowMillis: Long = 500): Flow<T> = flow {
    var lastEmitTime = 0L
    collect { value ->
        val now = BrickTimeSource.elapsedRealtimeMillis()
        if (now - lastEmitTime >= windowMillis) {
            lastEmitTime = now
            emit(value)
        }
    }
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.debounceAction(timeoutMillis: Long = 300): Flow<T> = debounce(timeoutMillis)

fun View.throttleClicks(intervalMillis: Long = 500): Flow<Unit> = callbackFlow {
    var lastClickTime = 0L
    setOnClickListener {
        val now = BrickTimeSource.elapsedRealtimeMillis()
        if (now - lastClickTime >= intervalMillis) {
            lastClickTime = now
            trySend(Unit)
        }
    }
    awaitClose { setOnClickListener(null) }
}

fun <T, R> Flow<T>.select(selector: (T) -> R): Flow<R> =
    map(selector).distinctUntilChanged()
