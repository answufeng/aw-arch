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

/**
 * ViewModel 基类，提供协程能力和 SavedStateHandle 支持。
 *
 * 所有 ViewModel 的公共父类，封装了：
 * - 协程启动（[launch]/[launchIO]/[launchDefault]）+ 自动异常处理
 * - 线程切换（[withMain]）
 * - SavedStateHandle 读写（[getSavedState]/[setSavedState]/[savedStateFlow]）
 *
 * ```kotlin
 * class MyViewModel : BaseViewModel() {
 *     fun loadData() = launchIO {
 *         val data = repository.fetch()
 *         withMain { updateUI(data) }
 *     }
 * }
 * ```
 *
 * @param savedStateHandle 进程重启后恢复状态，由 ViewModelProvider 自动注入
 */
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

    /** 在 Main 线程启动协程，异常由 [handleException] 或 [onError] 回调处理 */
    protected fun launch(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(resolveHandler(onError), block = block)
    }

    /** 在 IO 线程启动协程，适合网络请求、数据库操作等 */
    protected fun launchIO(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO + resolveHandler(onError), block = block)
    }

    /** 在 Default 线程启动协程，适合 CPU 密集型计算 */
    protected fun launchDefault(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Default + resolveHandler(onError), block = block)
    }

    /** 切换到 Main 线程执行，通常用于在 IO 协程中更新 UI */
    protected suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Main, block)

    /** 从 SavedStateHandle 读取值 */
    protected inline fun <reified T> getSavedState(key: String): T? =
        savedStateHandle?.get<T>(key)

    /** 向 SavedStateHandle 写入值 */
    protected fun <T> setSavedState(key: String, value: T) {
        savedStateHandle?.set(key, value)
    }

    /** 从 SavedStateHandle 获取 StateFlow，进程重启后自动恢复 */
    protected fun <T> savedStateFlow(key: String, initialValue: T): kotlinx.coroutines.flow.StateFlow<T> =
        savedStateHandle?.getStateFlow(key, initialValue)
            ?: kotlinx.coroutines.flow.MutableStateFlow(initialValue)

    /** 全局异常处理，默认通过 [AwArch.logger] 记录日志，子类可覆写自定义处理 */
    protected open fun handleException(throwable: Throwable) {
        AwArch.logger.e(this::class.simpleName ?: "BaseViewModel", "Unhandled exception", throwable)
    }
}
