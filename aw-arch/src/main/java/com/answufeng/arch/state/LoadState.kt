package com.answufeng.arch.state

import com.answufeng.arch.config.AwArch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeout

/**
 * 通用加载状态密封类，适用于任何异步数据加载场景。
 *
 * 三种基本状态：[Loading]（加载中）、[Success]（成功）、[Error]（失败）。
 *
 * ### 基本用法
 * ```kotlin
 * // 在 MVI State 中
 * data class HomeState(
 *     val items: LoadState<List<String>> = LoadState.Loading
 * ) : UiState
 *
 * // 在 ViewModel 中
 * updateState { copy(items = LoadState.Loading) }
 * try {
 *     val data = repository.fetchItems()
 *     updateState { copy(items = LoadState.Success(data)) }
 * } catch (e: Exception) {
 *     updateState { copy(items = LoadState.Error(e)) }
 * }
 *
 * // 在 UI 中渲染
 * when (val items = state.items) {
 *     is LoadState.Loading -> showProgressBar()
 *     is LoadState.Success -> adapter.submitList(items.data)
 *     is LoadState.Error   -> showError(items.message)
 * }
 * ```
 *
 * ### 操作符
 * ```kotlin
 * val result: LoadState<List<Item>> = LoadState.Success(items)
 *
 * // 转换成功数据
 * val names = result.map { it.map { item -> item.name } }
 *
 * // 安全取值
 * val data = result.getOrNull()
 * val data = result.getOrDefault(emptyList())
 *
 * // 模式匹配
 * result.fold(
 *     onLoading = { showLoading() },
 *     onSuccess = { render(it) },
 *     onError   = { showError(it) }
 * )
 *
 * // 在协程中快速包装
 * val state = loadStateCatching { repository.fetchItems() }
 * ```
 *
 * @param T 数据类型
 */
sealed class LoadState<out T> {

    /** 加载中 */
    data object Loading : LoadState<Nothing>()

    /** 加载成功 */
    data class Success<T>(val data: T) : LoadState<T>()

    /** 加载失败 */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "未知错误"
    ) : LoadState<Nothing>()

    /** 是否正在加载 */
    val isLoading: Boolean get() = this is Loading

    /** 是否加载成功 */
    val isSuccess: Boolean get() = this is Success

    /** 是否加载失败 */
    val isError: Boolean get() = this is Error
}

/**
 * 对 [LoadState.Success] 中的数据进行转换。
 *
 * 其他状态原样返回。
 */
fun <T, R> LoadState<T>.map(transform: (T) -> R): LoadState<R> = when (this) {
    is LoadState.Loading -> LoadState.Loading
    is LoadState.Success -> LoadState.Success(transform(data))
    is LoadState.Error -> this
}

/**
 * 取出成功数据，失败和加载中返回 null。
 */
fun <T> LoadState<T>.getOrNull(): T? = when (this) {
    is LoadState.Success -> data
    else -> null
}

/**
 * 取出成功数据，失败和加载中返回默认值。
 */
fun <T> LoadState<T>.getOrDefault(default: T): T = when (this) {
    is LoadState.Success -> data
    else -> default
}

/**
 * 三态模式匹配，覆盖所有分支。
 */
inline fun <T, R> LoadState<T>.fold(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (Throwable) -> R
): R = when (this) {
    is LoadState.Loading -> onLoading()
    is LoadState.Success -> onSuccess(data)
    is LoadState.Error -> onError(exception)
}

/**
 * 成功时执行回调。
 */
inline fun <T> LoadState<T>.onSuccess(action: (T) -> Unit): LoadState<T> {
    if (this is LoadState.Success) action(data)
    return this
}

/**
 * 失败时执行回调。
 */
inline fun <T> LoadState<T>.onError(action: (Throwable) -> Unit): LoadState<T> {
    if (this is LoadState.Error) action(exception)
    return this
}

/**
 * 加载中时执行回调。
 */
inline fun <T> LoadState<T>.onLoading(action: () -> Unit): LoadState<T> {
    if (this is LoadState.Loading) action()
    return this
}

/**
 * 对任意状态执行回调，适合统一处理（如日志、埋点）。
 */
inline fun <T> LoadState<T>.onEach(action: (LoadState<T>) -> Unit): LoadState<T> {
    action(this)
    return this
}

/**
 * 在协程中安全执行并包装为 [LoadState]。
 *
 * ```kotlin
 * val state = loadStateCatching { repository.fetchItems() }
 * ```
 */
suspend fun <T> loadStateCatching(block: suspend () -> T): LoadState<T> {
    return try {
        LoadState.Success(block())
    } catch (e: Exception) {
        AwArch.logger.e("LoadState", "loadStateCatching failed", e)
        LoadState.Error(e)
    }
}

/**
 * 在 [timeoutMillis] 内执行 [block]，超时则返回 [LoadState.Error]（[TimeoutCancellationException]）。
 *
 * ```kotlin
 * val state = loadStateWithTimeout(5_000) { repository.fetchItems() }
 * ```
 */
suspend fun <T> loadStateWithTimeout(
    timeoutMillis: Long,
    timeoutMessage: String = "请求超时",
    block: suspend () -> T,
): LoadState<T> {
    return try {
        LoadState.Success(withTimeout(timeoutMillis) { block() })
    } catch (e: TimeoutCancellationException) {
        AwArch.logger.w("LoadState", "loadStateWithTimeout: $timeoutMessage", e)
        LoadState.Error(e, timeoutMessage)
    } catch (e: Exception) {
        AwArch.logger.e("LoadState", "loadStateWithTimeout failed", e)
        LoadState.Error(e)
    }
}

/**
 * 带重试的异步加载，失败后指数退避重试。
 *
 * ```kotlin
 * val state = retryLoadState(times = 3, initialDelayMillis = 1000) {
 *     repository.fetchItems()
 * }
 * ```
 *
 * @param times 最大重试次数（不含首次调用）
 * @param initialDelayMillis 首次重试延迟（毫秒）
 * @param factor 延迟递增因子（指数退避）
 * @param maxDelayMillis 最大重试延迟上限（毫秒），默认 30 秒，防止延迟无限增长
 * @param block 要执行的挂起函数
 */
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
            AwArch.logger.w(
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
        AwArch.logger.e("LoadState", "retryLoadState exhausted all $times retries", e)
        LoadState.Error(e)
    }
}

fun <T> LoadState<T>.recover(defaultValue: T): LoadState<T> = when (this) {
    is LoadState.Error -> LoadState.Success(defaultValue)
    else -> this
}

fun <T> LoadState<T>.recoverWith(fn: (Throwable) -> T): LoadState<T> = when (this) {
    is LoadState.Error -> LoadState.Success(fn(exception))
    else -> this
}

fun <T, R> LoadState<T>.combine(other: LoadState<R>): LoadState<Pair<T, R>> = when {
    this is LoadState.Loading || other is LoadState.Loading -> LoadState.Loading
    this is LoadState.Error -> LoadState.Error(this.exception, this.message)
    other is LoadState.Error -> LoadState.Error(other.exception, other.message)
    this is LoadState.Success && other is LoadState.Success -> LoadState.Success(this.data to other.data)
    else -> LoadState.Loading
}

fun <T> Flow<T>.asLoadState(): Flow<LoadState<T>> {
    return this
        .map<T, LoadState<T>> { LoadState.Success(it) }
        .onStart { emit(LoadState.Loading) }
        .catch { emit(LoadState.Error(it)) }
}

fun <T, R> Flow<LoadState<T>>.mapLoadState(transform: (T) -> R): Flow<LoadState<R>> {
    return map { it.map(transform) }
}
