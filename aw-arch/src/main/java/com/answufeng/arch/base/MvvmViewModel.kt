package com.answufeng.arch.base

/**
 * MVVM 模式的 ViewModel 基类。
 *
 * 提供：
 * - 协程快捷启动：launch / launchIO / launchDefault
 * - 通用 UI 事件：showToast / showLoading / navigate / navigateBack
 * - SavedStateHandle 便捷方法
 * - 线程切换 withMain
 *
 * ### 子类示例
 * ```kotlin
 * class HomeViewModel(
 *     private val repository: HomeRepository,
 *     savedStateHandle: SavedStateHandle
 * ) : MvvmViewModel(savedStateHandle) {
 *     fun loadData() = launchIO {
 *         showLoading(true)
 *         val data = repository.fetchItems()
 *         showLoading(false)
 *         showToast("Data loaded!")
 *     }
 * }
 * ```
 */
open class MvvmViewModel(
    savedStateHandle: androidx.lifecycle.SavedStateHandle? = null
) : BaseViewModel(savedStateHandle)