package com.answufeng.arch.state

import androidx.compose.runtime.Immutable
import com.answufeng.arch.config.BrickArch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Immutable
sealed class LoadState<out T> {

    data object Loading : LoadState<Nothing>()

    data class Success<T>(val data: T) : LoadState<T>()

    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "未知错误"
    ) : LoadState<Nothing>()

    val isLoading: Boolean get() = this is Loading

    val isSuccess: Boolean get() = this is Success

    val isError: Boolean get() = this is Error
}

fun <T, R> LoadState<T>.map(transform: (T) -> R): LoadState<R> = when (this) {
    is LoadState.Loading -> LoadState.Loading
    is LoadState.Success -> LoadState.Success(transform(data))
    is LoadState.Error -> this
}

fun <T> LoadState<T>.getOrNull(): T? = when (this) {
    is LoadState.Success -> data
    else -> null
}

fun <T> LoadState<T>.getOrDefault(default: T): T = when (this) {
    is LoadState.Success -> data
    else -> default
}

inline fun <T, R> LoadState<T>.fold(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (Throwable) -> R
): R = when (this) {
    is LoadState.Loading -> onLoading()
    is LoadState.Success -> onSuccess(data)
    is LoadState.Error -> onError(exception)
}

inline fun <T> LoadState<T>.onSuccess(action: (T) -> Unit): LoadState<T> {
    if (this is LoadState.Success) action(data)
    return this
}

inline fun <T> LoadState<T>.onError(action: (Throwable) -> Unit): LoadState<T> {
    if (this is LoadState.Error) action(exception)
    return this
}

inline fun <T> LoadState<T>.onLoading(action: () -> Unit): LoadState<T> {
    if (this is LoadState.Loading) action()
    return this
}

fun <T> LoadState<T>.recover(defaultValue: T): LoadState<T> = when (this) {
    is LoadState.Loading -> LoadState.Loading
    is LoadState.Success -> this
    is LoadState.Error -> LoadState.Success(defaultValue)
}

inline fun <T> LoadState<T>.recoverWith(recover: (Throwable) -> T): LoadState<T> = when (this) {
    is LoadState.Loading -> LoadState.Loading
    is LoadState.Success -> this
    is LoadState.Error -> LoadState.Success(recover(exception))
}

fun <T, R, U> LoadState<T>.combine(other: LoadState<R>, transform: (T, R) -> U): LoadState<U> = when {
    this is LoadState.Loading || other is LoadState.Loading -> LoadState.Loading
    this is LoadState.Error -> LoadState.Error(this.exception, this.message)
    other is LoadState.Error -> LoadState.Error(other.exception, other.message)
    this is LoadState.Success && other is LoadState.Success -> LoadState.Success(transform(this.data, other.data))
    else -> LoadState.Loading
}

suspend fun <T> loadStateCatching(block: suspend () -> T): LoadState<T> {
    return try {
        LoadState.Success(block())
    } catch (e: Exception) {
        BrickArch.logger.e("LoadState", "loadStateCatching failed", e)
        LoadState.Error(e)
    }
}

suspend fun <T> retryLoadState(
    times: Int = 3,
    initialDelayMillis: Long = 1000,
    factor: Double = 2.0,
    maxDelayMillis: Long = 30_000L,
    block: suspend () -> T
): LoadState<T> {
    var currentDelay = initialDelayMillis.coerceAtMost(maxDelayMillis)
    repeat(times) { attempt ->
        try {
            return LoadState.Success(block())
        } catch (e: Exception) {
            BrickArch.logger.w(
                "LoadState",
                "retryLoadState attempt ${attempt + 1}/$times failed, retrying in ${currentDelay}ms",
                e
            )
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
    }
    return try {
        LoadState.Success(block())
    } catch (e: Exception) {
        BrickArch.logger.e("LoadState", "retryLoadState exhausted all $times retries", e)
        LoadState.Error(e)
    }
}

fun <T, R> Flow<LoadState<T>>.mapLoadState(transform: (T) -> R): Flow<LoadState<R>> =
    map { it.map(transform) }

fun <T> Flow<T>.asLoadState(): Flow<LoadState<T>> =
    map<T, LoadState<T>> { LoadState.Success(it) }
        .onStart { emit(LoadState.Loading) }
        .catch { emit(LoadState.Error(it as? Exception ?: RuntimeException(it))) }
