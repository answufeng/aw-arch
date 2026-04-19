package com.answufeng.arch.mvi

import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import com.answufeng.arch.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * MVI 模式 ViewModel 基类，管理 [UiState]/[UiEvent]/[UiIntent] 三层抽象。
 *
 * - **State**: 通过 [state] 暴露，UI 订阅后自动渲染
 * - **Event**: 一次性事件（如 Toast、导航），通过 [event] 暴露
 * - **Intent**: 用户意图，通过 [dispatch] 分发，由 [handleIntent] 处理
 *
 * ```kotlin
 * class CounterViewModel : MviViewModel<CounterState, CounterEvent, CounterIntent>(CounterState()) {
 *     override fun handleIntent(intent: CounterIntent) {
 *         when (intent) {
 *             CounterIntent.Increment -> updateState { copy(count = count + 1) }
 *             CounterIntent.LoadData -> loadData()
 *         }
 *     }
 *
 *     private fun loadData() = launchIO {
 *         updateState { copy(isLoading = true) }
 *         val data = repository.fetch()
 *         updateState { copy(isLoading = false, count = data.count) }
 *         sendMviEvent(CounterEvent.ShowSnackbar("加载完成"))
 *     }
 * }
 * ```
 *
 * @param S 状态类型，必须实现 [UiState]
 * @param E 事件类型，必须实现 [UiEvent]
 * @param I 意图类型，必须实现 [UiIntent]
 * @param initialState 初始状态
 * @param savedStateHandle 进程重启后恢复状态
 */
abstract class MviViewModel<S : UiState, E : UiEvent, I : UiIntent>(
    initialState: S,
    savedStateHandle: SavedStateHandle? = null
) : BaseViewModel(savedStateHandle) {

    private val _state = MutableStateFlow(initialState)

    /** 当前 UI 状态流，UI 层通过 `collect` 订阅渲染 */
    val state: StateFlow<S> = _state.asStateFlow()

    /** 获取当前状态快照，不触发收集 */
    protected val currentState: S get() = _state.value

    private val _event = Channel<E>(Channel.UNLIMITED)

    /** 一次性事件流（如 Toast、导航），消费后不会重放 */
    val event: Flow<E> = _event.receiveAsFlow()

    private val intentThrottleMap = HashMap<String, Long>()

    /** 可替换的时间源，用于节流计算。测试中可覆写以控制时间 */
    protected open fun currentTimeMillis(): Long = SystemClock.elapsedRealtime()

    /** 处理用户意图，子类必须实现 */
    abstract fun handleIntent(intent: I)

    /** 分发意图，直接调用 [handleIntent]。必须在主线程调用 */
    fun dispatch(intent: I) {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "dispatch() must be called on the main thread"
        }
        handleIntent(intent)
    }

    /**
     * 节流分发意图，同一 key 的 Intent 在 [windowMillis] 时间窗口内只处理一次。
     *
     * 默认节流 key 为 Intent 的类名（`intent::class.java.name`），
     * 因此同类型不同值的 Intent 会被一起节流。
     * 可通过 [keySelector] 自定义 key 策略。
     *
     * @param keySelector 自定义节流 key 函数，默认使用 Intent 类名
     */
    fun dispatchThrottled(intent: I, windowMillis: Long = 300, keySelector: (I) -> String = { it::class.java.name }) {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "dispatchThrottled() must be called on the main thread"
        }
        val key = keySelector(intent)
        val now = currentTimeMillis()
        val last = intentThrottleMap[key]
        if (last == null || now - last >= windowMillis) {
            intentThrottleMap[key] = now
            handleIntent(intent)
        }
    }

    /** 清除节流缓存，允许之前被节流的 Intent 重新分发 */
    protected fun clearThrottleCache() {
        intentThrottleMap.clear()
    }

    /** 更新状态，使用原子操作确保线程安全 */
    protected fun updateState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    /** 发送一次性事件，适合 Toast、导航等不需要状态持久化的场景 */
    protected fun sendMviEvent(event: E) {
        _event.trySend(event)
    }

    override fun onCleared() {
        super.onCleared()
        intentThrottleMap.clear()
    }
}
