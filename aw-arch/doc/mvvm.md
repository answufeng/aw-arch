# MVVM 模式

aw-arch 提供完整的 MVVM 架构基类，基于 `MvvmViewModel` + `MvvmActivity/Fragment/Dialog` 等基类，内置 `UiEvent` 通道实现一次性事件分发。

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
└── MvvmViewModel      ← + UiEvent（Toast / Loading / navigate）
```

## BaseViewModel

所有 ViewModel 的公共父类，封装了：

- 协程启动（`launch` / `launchIO` / `launchDefault`）+ 自动异常处理
- 线程切换（`withMain`）
- SavedStateHandle 读写（`getSavedState` / `setSavedState` / `savedStateFlow`）

```kotlin
class MyViewModel : BaseViewModel() {
    fun loadData() = launchIO {
        val data = repository.fetch()
        withMain { updateUI(data) }
    }
}
```

### 协程方法

| 方法 | 调度器 | 用途 |
|------|--------|------|
| `launch(onError?)` | Main | UI 相关操作 |
| `launchIO(onError?)` | IO | 网络请求、数据库操作 |
| `launchDefault(onError?)` | Default | CPU 密集型计算 |
| `withMain { }` | Main | 切回主线程更新 UI |

### SavedStateHandle

```kotlin
class MyViewModel(savedStateHandle: SavedStateHandle?) : BaseViewModel(savedStateHandle) {
    private val userId = savedStateFlow("user_id", "")
    fun setUserId(id: String) = setSavedState("user_id", id)
}
```

## MvvmViewModel

继承 `BaseViewModel`，增加 `UiEvent` 通道，向 UI 层发送一次性事件。

### 内置事件

| 事件 | 方法 | 说明 |
|------|------|------|
| `UiEvent.Toast` | `showToast(message)` | 显示 Toast |
| `UiEvent.Loading` | `showLoading(show)` | 显示/隐藏 Loading |
| `UiEvent.Navigate` | `navigate(route, extras)` | 导航到指定路由 |
| `UiEvent.NavigateBack` | `navigateBack()` | 返回上一页 |
| `UiEvent.Custom` | `sendEvent(UiEvent.Custom(key, data))` | 自定义事件 |

### 自定义事件

```kotlin
// 继承 UiEvent
sealed class MyEvent : MvvmViewModel.UiEvent() {
    data class ShowDialog(val title: String) : MyEvent()
}

class MyViewModel : MvvmViewModel() {
    fun triggerDialog() = sendEvent(MyEvent.ShowDialog("确认删除？"))
}
```

## 基类列表

| 基类 | 容器 | 特性 |
|------|------|------|
| `MvvmActivity<VB, VM>` | AppCompatActivity | ViewBinding + ViewModel 自动创建 |
| `MvvmFragment<VB, VM>` | Fragment | + 懒加载 + 可共享 Activity ViewModel |
| `MvvmDialogFragment<VB, VM>` | DialogFragment | 对话框场景 |
| `MvvmBottomSheetDialogFragment<VB, VM>` | BottomSheetDialogFragment | 底部弹窗场景 |

## 快速上手

```kotlin
class CounterViewModel : MvvmViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun inc() = launch { _count.value++ }
    fun loadData() = launchIO {
        showLoading(true)
        val data = repository.fetch()
        showLoading(false)
        showToast("加载完成")
    }
}

class CounterActivity : MvvmActivity<ActivityCounterBinding, CounterViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { viewModel.inc() }
    }

    override fun initObservers() {
        super.initObservers()
        viewModel.count.collectOnLifecycle(this) { binding.tvCount.text = it.toString() }
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
```

## AwNav 集成

在 MVVM 基类中覆写 `awNav` 属性，`UiEvent.Navigate` / `UiEvent.NavigateBack` 将自动交给 AwNav 处理：

```kotlin
class MainActivity : MvvmActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var nav: AwNav

    override val awNav: AwNav get() = nav

    override fun onCreate(savedInstanceState: Bundle?) {
        nav = AwNav.init(this, R.id.container)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
        super.onCreate(savedInstanceState)
    }
}
```

## 与 Activity 共享 ViewModel

```kotlin
class SharedFragment : MvvmFragment<FragmentSharedBinding, SharedViewModel>() {
    override val shareViewModelWithActivity: Boolean = true
}
```

## Hilt 版本

使用 `HiltMvvmActivity` / `HiltMvvmFragment`，ViewModel 通过 `by viewModels()` 注入：

```kotlin
@AndroidEntryPoint
class HiltDemoActivity : HiltMvvmActivity<ActivityHiltDemoBinding, HiltDemoViewModel>() {
    override val viewModel: HiltDemoViewModel by viewModels()
    override fun inflateBinding(inflater: LayoutInflater) = ActivityHiltDemoBinding.inflate(inflater)
    override fun initView(savedInstanceState: Bundle?) { ... }
}
```

## 注意事项

- `UiEvent` 通道容量为 128，满时丢弃最旧未消费事件
- `initObservers()` 默认已在 `STARTED` 状态收集 `uiEvent`，子类覆写时需调用 `super.initObservers()`
- `MvvmViewModel.UiEvent` 与 MVI 的 `UiEvent` 标记接口是不同的类型，注意区分
