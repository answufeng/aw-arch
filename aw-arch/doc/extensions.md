# Flow 扩展 & 生命周期扩展

aw-arch 提供了一系列 Flow 操作符和生命周期感知扩展，简化协程与 Android 生命周期的集成。

## Flow 操作符

### collectOnLifecycle

在 LifecycleOwner 的指定生命周期状态下收集 Flow：

```kotlin
viewModel.count
    .collectOnLifecycle(this) { value ->
        binding.tvCount.text = value.toString()
    }

// 指定生命周期状态
viewModel.count
    .collectOnLifecycle(this, Lifecycle.State.RESUMED) { value ->
        binding.tvCount.text = value.toString()
    }
```

### throttleFirst

在时间窗口内只发射第一个事件，适用于按钮点击防抖：

```kotlin
searchTrigger
    .throttleFirst(300)
    .collect { query -> search(query) }
```

与 `debounce` 的区别：throttle 在窗口期**开始**时立即发射，debounce 在窗口期**结束**时发射。

### debounceAction

防抖操作，在指定时间内无新事件才发射最后一个：

```kotlin
searchInput
    .debounceAction(300)
    .collect { query -> search(query) }
```

### select

从 StateFlow/Flow 中选择子字段，仅在字段值变化时发射：

```kotlin
// 从 StateFlow 选择
viewModel.state
    .select { it.count }
    .collect { count -> render(count) }

// 从 Flow 选择
viewModel.stateFlow
    .select { it.isLoading }
    .distinctUntilChanged()
    .collect { loading -> toggleProgressBar(loading) }
```

### throttleClicks

View 点击事件节流 Flow：

```kotlin
binding.btnSubmit
    .throttleClicks(300)
    .collectOnLifecycle(this) {
        submit()
    }
```

### observeMvi

MVI 架构的统一观察入口，同时订阅 State 和 Event：

```kotlin
observeMvi(
    viewModel.state,
    viewModel.event,
    render = { state -> render(state) },
    handleEvent = { event -> handleEvent(event) }
)
```

## 生命周期扩展

### observeEvent

观察 FlowEventBus 中的普通事件：

```kotlin
// reified 版本（推荐）
observeEvent<LoginSuccessEvent> { event ->
    updateUI(event.userId)
}

// KClass 版本
observeEvent(LoginSuccessEvent::class) { event ->
    updateUI(event.userId)
}
```

### observeStickyEvent

观察 FlowEventBus 中的粘性事件：

```kotlin
observeStickyEvent<ThemeChangedEvent> { event ->
    applyTheme(event.darkMode)
}
```

### launchOnStarted

在 STARTED 状态启动协程，离开 STARTED 自动取消：

```kotlin
launchOnStarted {
    repository.observeData().collect { data ->
        render(data)
    }
}
```

### launchOnResumed

在 RESUMED 状态启动协程，离开 RESUMED 自动取消：

```kotlin
launchOnResumed {
    // 仅在 RESUMED 时执行
    updateLocation()
}
```

## ViewBinding 委托

除基类内置的 ViewBinding 管理外，还提供独立的属性委托方式。

### Fragment 委托

```kotlin
class MyFragment : Fragment() {
    private val binding by viewBinding(MyFragmentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = "Hello"
    }
}
```

自动在 `onDestroyView` 时置空，无需手动管理。

### Activity 委托

```kotlin
class MyActivity : AppCompatActivity() {
    private val binding by viewBinding(MyActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 首次访问 binding 时自动 setContentView
        binding.tvTitle.text = "Hello"
    }
}
```

## ViewModel 反射推断

`inferViewModelClass` 通过反射推断继承链上父类泛型参数中的 ViewModel 类型，基类自动调用。当无法推断时（如使用了星投影 `*`），需覆写 `createViewModel()`：

```kotlin
// 无法推断时手动创建
override fun createViewModel(): MyViewModel {
    return ViewModelProvider(this).get(MyViewModel::class.java)
}
```

## 注意事项

- `collectOnLifecycle` 默认在 `STARTED` 状态收集，与 `repeatOnLifecycle` 行为一致
- `throttleFirst` 的首个元素始终会发射（不依赖时间戳绝对值大小）
- `FragmentViewBindingDelegate` 在 `onDestroyView` 时自动置空 binding，访问已销毁的 binding 会抛 `IllegalStateException`
- `ActivityViewBindingDelegate` 首次访问时自动 `setContentView`，不适用于需要自定义 `setContentView` 逻辑的场景
