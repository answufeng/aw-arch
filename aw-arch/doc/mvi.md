# MVI 模式

aw-arch 提供完整的 MVI 架构基类，基于 `MviViewModel<State, Effect, Intent>` + 对应 Activity/Fragment/Dialog 基类，实现严格单向数据流。

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
└── MviViewModel       ← + State / Effect / Intent
    └── SimpleMviViewModel  ← 简化（无独立 Effect 类型）
```

## 核心概念

```
用户操作 → Intent → ViewModel.handleIntent() → updateState / sendMviEvent
                    ↓                              ↓
              State (StateFlow)              Effect (Channel)
                    ↓                              ↓
              UI render(state)              UI handleEvent(effect)
```

- **State**：屏幕完整 UI 状态快照，通过 `StateFlow` 暴露，UI 订阅后自动渲染
- **Effect**：一次性副作用（Toast、导航等），消费后不会重放；标记接口为 [`MviEffect`](../../src/main/java/com/answufeng/arch/mvi/MviEffect.kt)
- **Intent**：用户意图，通过 `dispatch` 分发

> 历史名称 `com.answufeng.arch.mvi.UiEvent` 为 `MviEffect` 的 deprecated typealias，勿与 `MvvmViewModel.UiEvent` 混淆。

## MviViewModel

```kotlin
data class CounterState(val count: Int = 0, val isLoading: Boolean = false) : UiState

sealed class CounterEffect : MviEffect {
    data class ShowMessage(val message: String) : CounterEffect()
}

sealed class CounterIntent : UiIntent {
    data object Increment : CounterIntent()
    data object LoadData : CounterIntent()
}

class CounterViewModel : MviViewModel<CounterState, CounterEffect, CounterIntent>(CounterState()) {
    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> updateState { copy(count = count + 1) }
            CounterIntent.LoadData -> loadData()
        }
    }

    private fun loadData() = launchIO {
        updateState { copy(isLoading = true) }
        val data = repository.fetch()
        updateState { copy(isLoading = false, count = data.count) }
        sendMviEvent(CounterEffect.ShowMessage("加载完成"))
    }
}
```

### 核心 API

| 方法 | 说明 |
|------|------|
| `updateState { copy(...) }` | 原子更新状态 |
| `sendMviEvent(effect)` | 发送一次性 Effect |
| `currentState` | 获取当前状态快照 |
| `dispatch(intent)` | 分发意图（主线程） |
| `dispatchThrottled(intent, windowMillis)` | 节流分发，同一 key 在窗口期内只处理一次 |

### MviDispatcher 接口

Activity/Fragment 基类通过实现 `MviDispatcher<INTENT>` 接口获得 `dispatch` 能力：

```kotlin
interface MviDispatcher<INTENT : UiIntent> {
    fun dispatch(intent: INTENT)
    fun dispatchThrottled(intent: INTENT, windowMillis: Long = 300, key: () -> String = { intent.key })
}
```

- `dispatch(intent)` — 将 Intent 发送给 ViewModel 处理，必须在主线程调用
- `dispatchThrottled(intent, windowMillis, key)` — 节流版 dispatch，同一 key 在指定时间窗口内只处理一次，防止快速重复点击

### dispatchThrottled

```kotlin
dispatchThrottled(CounterIntent.Increment)
dispatchThrottled(CounterIntent.Increment, windowMillis = 500) { "btn_increment" }
```

## 基类列表

### 标准 MVI（State + Effect + Intent）

| 基类 | 容器 | 泛型参数 |
|------|------|----------|
| `MviActivity<VB, S, E, I, VM>` | AppCompatActivity | 5 个（E 为 `MviEffect`） |
| `MviFragment<VB, S, E, I, VM>` | Fragment | 5 个 + 懒加载 |
| `MviDialogFragment<VB, S, E, I, VM>` | DialogFragment | 5 个 |
| `MviBottomSheetDialogFragment<VB, S, E, I, VM>` | BottomSheetDialogFragment | 5 个 |

### 简化 MVI（State + Intent，无 Effect）

| 基类 | 容器 | 泛型参数 |
|------|------|----------|
| `SimpleMviActivity<VB, S, I, VM>` | AppCompatActivity | 4 个 |
| `SimpleMviFragment<VB, S, I, VM>` | Fragment | 4 个 + 懒加载 |
| `SimpleMviDialogFragment<VB, S, I, VM>` | DialogFragment | 4 个 |
| `SimpleMviBottomSheetDialogFragment<VB, S, I, VM>` | BottomSheetDialogFragment | 4 个 |

## 快速上手

```kotlin
class CounterActivity : MviActivity<
    ActivityCounterBinding,
    CounterState,
    CounterEffect,
    CounterIntent,
    CounterViewModel
>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { dispatch(CounterIntent.Increment) }
        binding.btnLoad.setOnClickListener { dispatch(CounterIntent.LoadData) }
    }

    override fun render(state: CounterState) {
        binding.tvCount.text = state.count.toString()
        binding.progressBar.isVisible = state.isLoading
    }

    override fun handleEvent(event: CounterEffect) {
        when (event) {
            is CounterEffect.ShowMessage ->
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
        }
    }
}
```

## SimpleMviViewModel

不需要一次性 Effect 时，使用简化版：

```kotlin
class SimpleCounterVM : SimpleMviViewModel<SimpleCounterState, SimpleCounterIntent>(SimpleCounterState()) {
    override fun handleIntent(intent: SimpleCounterIntent) {
        when (intent) {
            SimpleCounterIntent.Inc -> updateState { copy(count = count + 1) }
        }
    }
}
```

## 与 Activity 共享 ViewModel

```kotlin
class SharedMviFragment : MviFragment<VB, S, E, I, VM>() {
    override val shareViewModelWithActivity: Boolean = true
}
```

## Hilt 版本

| 基类 | 说明 |
|------|------|
| `HiltMviActivity<VB, S, E, I, VM>` | ViewModel 通过 Hilt 注入 |
| `HiltMviFragment<VB, S, E, I, VM>` | 同上 + 懒加载 |
| `HiltSimpleMviActivity<VB, S, I, VM>` | 无 Effect 的 Hilt 版 |

```kotlin
@AndroidEntryPoint
class HiltMviDemoActivity : HiltMviActivity<VB, S, E, I, VM>() {
    override val viewModel: VM by viewModels()
    override fun inflateBinding(inflater: LayoutInflater) = ...
    override fun initView(savedInstanceState: Bundle?) { ... }
    override fun render(state: S) { ... }
}
```

## 注意事项

- `dispatch()` 和 `dispatchThrottled()` 必须在主线程调用
- `event` 通道容量为 128，满时丢弃最旧 Effect
- `SimpleMvi*` 的第四泛型参数 `VM` 必须是具体的 ViewModel 实现类，供反射创建
- `MviDispatcher<INTENT>` 接口由基类自动实现，Fragment/Activity 可直接调用 `dispatch()`
