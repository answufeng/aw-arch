package com.answufeng.arch.mvvm

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * MVVM 视图层辅助接口，提供 UIEvent 的生命周期安全收集与默认分发。
 *
 * 所有 MVVM 基类（[MvvmActivity]、[MvvmFragment] 等）都实现此接口，
 * 将重复的 collect 和事件分发逻辑集中到默认方法中，消除代码冗余。
 *
 * ### 默认事件处理
 * - [BaseViewModel.UIEvent.Toast] → 弹出 Toast
 * - [BaseViewModel.UIEvent.Loading] → 回调 [onLoading]
 * - [BaseViewModel.UIEvent.Navigate] → 回调 [onNavigate]
 * - [BaseViewModel.UIEvent.NavigateBack] → 回调 [onNavigateBack]
 * - [BaseViewModel.UIEvent.Custom] → 回调 [onCustomEvent]
 *
 * 子类可覆写 [onUIEvent] 定制行为，或覆写单个回调方法。
 */
interface MvvmView<VM : BaseViewModel> : LifecycleOwner {

    /** MVVM ViewModel 实例 */
    val mvvmViewModel: VM

    /**
     * 在 STARTED 状态自动收集 UIEvent。
     *
     * 应在视图创建完成后调用一次（Activity.onCreate / Fragment.onViewCreated）。
     */
    fun collectUIEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mvvmViewModel.uiEvent.collect { event -> onUIEvent(event) }
            }
        }
    }

    /**
     * 处理通用 UI 事件。
     *
     * 默认行为：Toast → 弹 Toast，Loading → 调 [onLoading]，
     * NavigateBack → 调 [onNavigateBack]。子类可覆写定制行为。
     */
    fun onUIEvent(event: BaseViewModel.UIEvent) {
        when (event) {
            is BaseViewModel.UIEvent.Toast -> showToast(event.message)
            is BaseViewModel.UIEvent.Loading -> onLoading(event.show)
            is BaseViewModel.UIEvent.NavigateBack -> onNavigateBack()
            is BaseViewModel.UIEvent.Navigate -> onNavigate(event.route, event.extras)
            is BaseViewModel.UIEvent.Custom -> onCustomEvent(event.key, event.data)
        }
    }

    /** 显示 Toast，子类可覆写以自定义 Toast 实现 */
    fun showToast(message: String) {
        Toast.makeText(viewContext, message, Toast.LENGTH_SHORT).show()
    }

    /** 获取 Context 用于 Toast 等操作 */
    val viewContext: Context

    /** Loading 事件回调，子类按需覆写 */
    fun onLoading(show: Boolean) {}

    /** 导航事件回调，子类按需覆写 */
    fun onNavigate(route: String, extras: Map<String, Any>?) {}

    /** 返回事件回调，子类按需覆写 */
    fun onNavigateBack() {}

    /** 自定义事件回调 */
    fun onCustomEvent(key: String, data: Any?) {}
}
