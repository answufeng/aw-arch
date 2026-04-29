# MVI 模式

aw-arch 提供完整的 MVI 架构基类，基于 `MviViewModel<State, Event, Intent>` + 对应 Activity/Fragment/Dialog 基类，实现严格单向数据流。

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
└── MviViewModel       ← + State / Event / Intent
    └── SimpleMviViewModel  ← 简化（NoEvent）
```

## 核心概念

```
用户操作 → Intent → ViewModel.handleIntent() → updateState / sendMviEvent
                    ↓                              ↓
              State (StateFlow)              Event (Channel)
                    ↓                              ↓
              UI render(state)              UI handleEvent(event)
```

- **State**：屏幕完整 UI 状态快照，通过 `StateFlow` 暴露，UI 订阅后自动渲染
- **Event**：一次性事件（Toast、导航等），消费后不会重放
- **Intent**：用户意图，通过 `dispatch` 分发

## MviViewModel

```kotlin
data class CounterState(val count: Int = 0, val isLoading: Boolean = false) : UiState

sealed class CounterEvent : UiEvent {
    data class ShowSnackbar(val message: String) : CounterEvent()
}

sealed class CounterIntent : UiIntent {
    data object Increment : CounterIntent()
    data object LoadData : CounterIntent()
}

class CounterViewModel : MviViewModel<CounterState, CounterEvent, CounterIntent>(CounterState()) {
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
        sendMviEvent(CounterEvent.ShowSnackbar("加载完成"))
    }
}
```

### 核心 API

| 方法 | 说明 |
|------|------|
| `updateState { copy(...) }` | 原子更新状态 |
| `sendMviEvent(event)` | 发送一次性事件 |
| `currentState` | 获取当前状态快照 |
| `dispatch(intent)` | 分发意图（主线程） |
| `dispatchThrottled(intent, windowMillis)` | 节流分发，同一 key 在窗口期内只处理一次 |

### dispatchThrottled

```kotlin
// 默认按 Intent 类名节流，300ms 窗口
dispatchThrottled(CounterIntent.Increment)

// 自定义 key 和窗口
dispatchThrottled(CounterIntent.Increment, windowMillis = 500) { "btn_increment" }
```

## 基类列表

### 标准 MVI（State + Event + Intent）

| 基类 | 容器 | 泛型参数 |
|------|------|----------|
| `MviActivity<VB, S, E, I, VM>` | AppCompatActivity | 5 个 |
| `MviFragment<VB, S, E, I, VM>` | Fragment | 5 个 + 懒加载 |
| `MviDialogFragment<VB, S, E, I, VM>` | DialogFragment | 5 个 |
| `MviBottomSheetDialogFragment<VB, S, E, I, VM>` | BottomSheetDialogFragment | 5 个 |

### 简化 MVI（State + Intent，无 Event）

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
    CounterEvent,
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

    override fun handleEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.ShowSnackbar -> Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
        }
    }
}
```

## SimpleMviViewModel

不需要一次性事件时，使用简化版：

```kotlin
class SimpleCounterVM : SimpleMviViewModel<SimpleCounterState, SimpleCounterIntent>(SimpleCounterState()) {
    override fun handleIntent(intent: SimpleCounterIntent) {
        when (intent) {
            SimpleCounterIntent.Inc -> updateState { copy(count = count + 1) }
        }
    }
}

class SimpleCounterActivity : SimpleMviActivity<
    ActivitySimpleCounterBinding,
    SimpleCounterState,
    SimpleCounterIntent,
    SimpleCounterVM
>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivitySimpleCounterBinding.inflate(inflater)
    override fun initView(savedInstanceState: Bundle?) { ... }
    override fun render(state: SimpleCounterState) { ... }
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
- `event` 通道容量为 128，满时丢弃最旧事件
- `SimpleMvi*` 的第四泛型参数 `VM` 必须是具体的 ViewModel 实现类，供反射创建
- `MviDispatcher<INTENT>` 接口由基类自动实现，Fragment/Activity 可直接调用 `dispatch()`
