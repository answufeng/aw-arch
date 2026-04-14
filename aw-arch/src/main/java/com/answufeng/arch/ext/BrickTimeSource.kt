package com.answufeng.arch.ext

import android.os.SystemClock

internal object BrickTimeSource {
    fun elapsedRealtimeMillis(): Long {
        return try {
            SystemClock.elapsedRealtime()
        } catch (_: Throwable) {
            System.nanoTime() / 1_000_000L
        }
    }
}
