# aw-arch

[![](https://jitpack.io/v/answufeng/aw-arch.svg)](https://jitpack.io/#answufeng/aw-arch)

Android 架构基础库，基于 Kotlin + MVVM/MVI + Hilt 封装，提供开箱即用的基类、导航管理、事件总线、Flow 扩展和状态管理。

**验证环境**：仓库内 **demo** 使用 compileSdk 35 / targetSdk 35（JDK 17、ViewBinding + Hilt）。

## 工程品质与发版检查

- **CI**：[`.github/workflows/ci.yml`](.github/workflows/ci.yml) — JDK 17 下 `:aw-arch:assembleRelease`、`ktlintCheck`、`lintRelease`、`:demo:assembleRelease`（R8 与 consumer 规则冒烟）。
- **本地建议（仓库根目录）**：`./gradlew :aw-arch:assembleRelease :aw-arch:ktlintCheck :aw-arch:lintRelease :demo:assembleRelease`
- **演示索引**：[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)；demo 主页 **「演示清单」** 可速览各 Activity 职责。
- **集成提示**：ViewModel 协程测试请在宿主侧使用 `kotlinx-coroutines-test` 自行替换 `Dispatchers.Main`（本库不再内置 JUnit Rule）。

## 五分钟上手（最小可复制）

1. **依赖**：`implementation("com.github.answufeng:aw-arch:…")` + 若用 Hilt：`hilt-android` + `ksp(hilt-compiler)`，模块启用 `viewBinding = true`。
2. **Application**：`@HiltAndroidApp class App : Application()`；在 `onCreate` 中可选 `AwArch.init { … }`。
3. **界面**：继承 `HiltMvvmActivity<YourVm, YourBinding>(R.layout.xxx)`（或 `MvvmActivity` / `MviActivity`），在 `onBindViewModel` 里订阅 `StateFlow` / `LiveData`；XML 根布局用标准 `ConstraintLayout` / `FragmentContainerView` 即可。

更完整的基类对照与导航示例见下文「快速开始」与 demo 工程。

## 特性

| 模块 | 说明 |
|---|---|
| **MVVM** | BaseViewModel + MvvmViewModel + MvvmActivity/Fragment/DialogFragment/BottomSheetDialogFragment |
| **MVI** | MviViewModel + MviActivity/Fragment/DialogFragment/BottomSheetDialogFragment |
| **SimpleMVI** | SimpleMviViewModel + SimpleMvi* 基类（4 个泛型参数含 VM，无需独立 Event 类型） |
| **Hilt** | HiltMvvm/MviActivity/Fragment/DialogFragment/BottomSheetDialogFragment，使用 `abstract val viewModel` 注入 |
| **AwNav** | 纯代码 Fragment 导航，支持动画、拦截器、回退栈、防连点 |
| **FlowEventBus** | 基于 SharedFlow 的事件总线，支持粘性事件、类型安全观察 |
| **LoadState** | Loading/Success/Error 密封类，支持重试、map、fold、recover、combine 等操作符 |
| **Flow 扩展** | throttleFirst、debounceAction、select、throttleClicks |
| **生命周期安全** | collectOnLifecycle、observeEvent、observeStickyEvent、launchOnStarted/Resumed |
| **ViewBinding 委托** | Activity/Fragment 属性委托，零反射，自动管理生命周期 |

## 引入

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-arch:2.0.0")
}
```

如果使用 Hilt 集成，还需添加 Hilt 插件和依赖：

```kotlin
// 项目级 build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android") version "2.52" apply false
}

// 模块级 build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
}
```

## 演示应用

仓库 `demo/` 提供 MVVM / MVI / `FlowEventBus` / `AwNav` / Hilt 等入口，索引见 [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)。

## 快速开始

### Step 1: Application 初始化

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 可选：配置自定义日志（默认使用 Android Log）
        AwArch.init {
            logger = object : AwLogger {
                override fun d(tag: String, message: String) = Log.d(tag, message)
                override fun w(tag: String, message: String, throwable: Throwable?) = Log.w(tag, message, throwable)
                override fun e(tag: String, message: String, throwable: Throwable?) = Log.e(tag, message, throwable)
            }
        }
    }
}
```

### Step 2: 创建 ViewModel

```kotlin
class CounterViewModel : MvvmViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() = launch {
        _count.value++
    }

    fun decrement() = launch {
        _count.value--
    }
}
```

### Step 3: 创建 Activity

布局文件 `activity_counter.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical">

    <TextView android:id="@+id/tvCount"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="48sp" />

    <Button android:id="@+id/btnDec" android:text="-"
        android:layout_width="wrap_content" android:layout_height="wrap_content" />
    <Button android:id="@+id/btnInc" android:text="+"
        android:layout_width="wrap_content" android:layout_height="wrap_content" />
</LinearLayout>
```

```kotlin
class CounterActivity : MvvmActivity<ActivityCounterBinding, CounterViewModel>() {
    override fun viewModelClass() = CounterViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { viewModel.increment() }
        binding.btnDec.setOnClickListener { viewModel.decrement() }
    }

    override fun initObservers() {
        super.initObservers()
        viewModel.count.collectOnLifecycle(this) { count ->
            binding.tvCount.text = count.toString()
        }
    }
}
```

以上就是一个完整的 MVVM 页面。如需使用 MVI 模式、Hilt 集成、导航等进阶功能，请继续阅读下方章节。

## ViewModel 层级

```
BaseViewModel          ← 协程能力（launch/launchIO/launchDefault/withMain/SavedStateHandle）
├── MvvmViewModel      ← + UiEvent（showToast/showLoading/navigate/navigateBack）
└── MviViewModel       ← + State/Event/Intent（updateState/sendMviEvent/dispatch/dispatchThrottled）
    └── SimpleMviViewModel  ← 简化版（NoEvent，减少泛型参数）
```

## MVVM 模式

### ViewModel

```kotlin
class HomeViewModel(
    private val repository: HomeRepository
) : MvvmViewModel() {

    private val _data = MutableStateFlow<List<Item>>(emptyList())
    val data: StateFlow<List<Item>> = _data.asStateFlow()

    fun loadData() = launchIO {
        val result = repository.fetchItems()
        _data.value = result
        showToast("加载完成")
    }

    fun deleteItem(id: String) = launchIO {
        repository.delete(id)
        showToast("已删除")
    }
}
```

### Activity

```kotlin
class HomeActivity : MvvmActivity<ActivityHomeBinding, HomeViewModel>() {
    override fun viewModelClass() = HomeViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityHomeBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnLoad.setOnClickListener { viewModel.loadData() }
    }

    override fun initObservers() {
        super.initObservers()
        viewModel.data.collectOnLifecycle(this) { items ->
            adapter.submitList(items)
        }
    }

    override fun onLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }
}
```

### Fragment

```kotlin
class HomeFragment : MvvmFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun viewModelClass() = HomeViewModel::class.java
    override val shareViewModelWithActivity = true

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) { /* ... */ }
}
```

## MVI 模式

### 定义 State / Event / Intent

```kotlin
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false
) : UiState

sealed class CounterEvent : UiEvent {
    data class ShowSnackbar(val message: String) : CounterEvent()
}

sealed class CounterIntent : UiIntent {
    data object Increment : CounterIntent()
    data object Decrement : CounterIntent()
    data object LoadData : CounterIntent()
}
```

### ViewModel

```kotlin
class CounterViewModel : MviViewModel<CounterState, CounterEvent, CounterIntent>(CounterState()) {

    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> updateState { copy(count = count + 1) }
            CounterIntent.Decrement -> updateState { copy(count = count - 1) }
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

### Activity

```kotlin
class CounterActivity : MviActivity<
    ActivityCounterBinding, CounterState, CounterEvent, CounterIntent, CounterViewModel
>() {
    override fun viewModelClass() = CounterViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnAdd.setOnClickListener { dispatch(CounterIntent.Increment) }
        binding.btnLoad.setOnClickListener { dispatch(CounterIntent.LoadData) }
    }

    override fun render(state: CounterState) {
        binding.tvCount.text = state.count.toString()
        binding.progressBar.isVisible = state.isLoading
    }

    override fun handleEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.ShowSnackbar ->
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
        }
    }
}
```

### 节流分发（防连点）

```kotlin
binding.btnSubmit.setOnClickListener {
    dispatchThrottled(SubmitIntent.Click, windowMillis = 500)
}
```

### 简化版 MVI（不需要自定义 Event）

```kotlin
class CounterViewModel : SimpleMviViewModel<CounterState, CounterIntent>(CounterState()) {
    override fun handleIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Increment -> updateState { copy(count = count + 1) }
            CounterIntent.Decrement -> updateState { copy(count = count - 1) }
        }
    }
}

class CounterActivity :
    SimpleMviActivity<ActivityCounterBinding, CounterState, CounterIntent, CounterViewModel>() {
    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)
    override fun initView(savedInstanceState: Bundle?) { /* ... */ }
    override fun render(state: CounterState) { /* ... */ }
}
```

## Hilt 集成

Hilt 基类使用 `abstract val viewModel` 由子类通过 `@Inject` 注入，无需 `viewModelClass()`：

```kotlin
@AndroidEntryPoint
class HomeActivity : HiltMvvmActivity<ActivityHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by viewModels()
    override fun inflateBinding(inflater: LayoutInflater) = ActivityHomeBinding.inflate(inflater)
    override fun initView(savedInstanceState: Bundle?) { /* ... */ }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : MvvmViewModel() {
    fun loadData() = launchIO { /* ... */ }
}
```

## AwNav 导航

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav = AwNav.init(this, R.id.container)
            .register<HomeFragment>("home")
            .register<ProfileFragment>("profile")
            .addInterceptor { from, to, _ ->
                if (to == "profile" && !userManager.isLoggedIn) {
                    nav.navigate("login")
                    false
                } else true
            }

        if (savedInstanceState == null) {
            nav.navigate("home") { addToBackStack = false; anim = NavAnim.NONE }
        }
    }
}
```

### 从 Fragment 中导航

```kotlin
AwNav.from(this).navigate("settings")
```

## FlowEventBus 事件总线

```kotlin
// 发送事件
FlowEventBus.post(LoginSuccessEvent("user123"))
FlowEventBus.tryPost(LoginSuccessEvent("user123"))
FlowEventBus.postSticky(ThemeChangedEvent(darkMode = true))
FlowEventBus.tryPostSticky(ThemeChangedEvent(darkMode = true))

// 接收事件
FlowEventBus.observe<LoginSuccessEvent>().collectOnLifecycle(this) { event ->
    updateUI(event.userId)
}

// 便捷扩展
observeEvent<LoginSuccessEvent> { event ->
    updateUI(event.userId)
}

// 移除粘性事件
FlowEventBus.removeSticky<ThemeChangedEvent>()
```

- **自动清理**：`FlowEventBus.autoCleanupDelay` 默认 30s；仅当某类型**曾经有过订阅者**且当前订阅数归零超过该时间后，才释放对应 `SharedFlow`。只调用 `observe()` / `observeSticky()` 取得 Flow、尚未开始 `collect` 时**不会**被清理。普通事件与粘性事件通道**分别**清理。设为 `0` 或负数可关闭自动清理。

## LoadState 状态管理

```kotlin
// 在 MVI State 中
data class HomeState(
    val items: LoadState<List<String>> = LoadState.Loading
) : UiState

// ViewModel 中
updateState { copy(items = LoadState.Loading) }
val result = loadStateCatching { repository.fetchItems() }
updateState { copy(items = result) }

// UI 中渲染
when (val items = state.items) {
    is LoadState.Loading -> showProgressBar()
    is LoadState.Success -> adapter.submitList(items.data)
    is LoadState.Error -> showError(items.message)
}

// 操作符
result.map { it.map { item -> item.name } }
result.getOrNull()
result.getOrDefault(emptyList())
result.recover(emptyList())
result.recoverWith { emptyList() }
result.combine(otherState) { a, b -> a to b }
result.fold(onLoading = {}, onSuccess = {}, onError = {})

// Flow 扩展
viewModel.dataFlow.asLoadState()
viewModel.loadStateFlow.mapLoadState { it.items }
```

## Flow 扩展

```kotlin
// 节流：500ms 内只发射第一个
viewModel.state.throttleFirst(500).collectOnLifecycle(this) { render(it) }

// 防抖：300ms 内没有新元素时才发射
searchFlow.debounceAction(300).collectOnLifecycle(this) { query -> viewModel.search(query) }

// 选择子字段并去重
viewModel.state.select { it.count }.collectOnLifecycle(this) { count -> binding.tvCount.text = count.toString() }

// View 点击防抖
binding.btnSubmit.throttleClicks(1000).collectOnLifecycle(this) { viewModel.submit() }
```

## 生命周期扩展

```kotlin
// 便捷事件观察
observeEvent<LoginSuccessEvent> { updateUI(it.userId) }
observeStickyEvent<ThemeChangedEvent> { applyTheme(it.darkMode) }

// 生命周期感知的协程启动
launchOnStarted { viewModel.state.collect { render(it) } }
launchOnResumed { refreshData() }
```

## 懒加载

MviFragment / MvvmFragment / BaseFragment 均支持懒加载，在 Fragment 首次可见时执行一次性初始化：

```kotlin
class HomeFragment : MviFragment<...>() {
    override fun onLazyLoad() {
        dispatch(HomeIntent.LoadData)
    }
}
```

- 懒加载仅在 Fragment 首次 `onResume` 时触发一次
- 配合 ViewPager 使用时，只有当前页会触发 `onLazyLoad`
- 进程重启后懒加载状态自动恢复

## BaseActivity

纯 ViewBinding 基类（不含 ViewModel），适合简单页面：

```kotlin
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateBinding() = ActivitySplashBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener { navigateToMain() }
    }
}
```

## ViewBinding 委托

```kotlin
// Activity
class HomeActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}

// Fragment
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::inflate)
}
```

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                           UI Layer                              │
│  MvvmActivity/Fragment/Dialog   MviActivity/Fragment/Dialog    │
│  SimpleMviActivity              HiltMvvm/MviActivity/Fragment  │
├─────────────────────────────────────────────────────────────────┤
│                        ViewModel Layer                          │
│  BaseViewModel (协程)                                            │
│  ├── MvvmViewModel (+UiEvent)                                   │
│  └── MviViewModel (+State/Event/Intent)                         │
│      └── SimpleMviViewModel (NoEvent)                           │
├─────────────────────────────────────────────────────────────────┤
│                       Infrastructure                            │
│  AwNav  │  FlowEventBus  │  LoadState  │  FlowExt  │  VBDelegate│
│  LifecycleExt  │  AwArch/Logger                                  │
└─────────────────────────────────────────────────────────────────┘
```

## API 速览

### BaseViewModel

| 方法 | 说明 |
|---|---|
| `launch(onError?, block)` | 启动协程，自动异常处理 |
| `launchIO(onError?, block)` | IO 线程协程 |
| `launchDefault(onError?, block)` | Default 线程协程 |
| `withMain(block)` | 切换到主线程 |
| `handleException(throwable)` | 异常处理（可覆写） |

### MvvmViewModel

| 方法 | 说明 |
|---|---|
| `sendEvent(event)` | 发送 UiEvent |
| `showToast(message)` | 弹 Toast |
| `showLoading(show)` | 显示/隐藏 Loading |
| `navigate(route, extras)` | 触发导航 |
| `navigateBack()` | 触发返回 |

### MviViewModel

| 方法 | 说明 |
|---|---|
| `dispatch(intent)` | 分发 Intent |
| `dispatchThrottled(intent, windowMillis)` | 节流分发 |
| `updateState { reduce }` | 更新 State |
| `sendMviEvent(event)` | 发送一次性事件 |
| `clearThrottleCache()` | 清除节流缓存 |

### AwNav

| 方法 | 说明 |
|---|---|
| `init(activity, containerId)` | 初始化导航 |
| `register<F>(route)` | 注册路由 |
| `addInterceptor(interceptor)` | 添加拦截器 |
| `navigate(route, args, builder)` | 导航 |
| `back()` | 返回上一页 |
| `backTo(route, inclusive)` | 弹出到指定路由 |
| `clearStack()` | 清空返回栈 |
| `from(fragment/activity)` | 获取实例 |

### FlowEventBus

| 方法 | 说明 |
|---|---|
| `post(event)` | 发送事件（挂起） |
| `tryPost(event)` | 发送事件（非挂起，tryEmit） |
| `postSticky(event)` | 发送粘性事件 |
| `tryPostSticky(event)` | 发送粘性事件（非挂起） |
| `observe<T>()` | 观察类型安全事件流 |
| `observeSticky<T>()` | 观察粘性事件流 |
| `removeSticky<T>()` | 移除粘性事件 |
| `clear(clazz)` | 清除指定通道 |
| `clearAll()` | 清除所有通道 |

## 迁移指南（1.x → 2.0）

1. `BaseViewModel` → `MvvmViewModel`：MVVM 模式的 ViewModel 需从 `BaseViewModel` 迁移到 `MvvmViewModel`
2. `BaseViewModel.UiEvent` → `MvvmViewModel.UiEvent`：UiEvent 类移至 MvvmViewModel 内部
3. `MviViewModel` 不再有 `showToast`/`showLoading`/`navigate`/`navigateBack` 方法，改用 `sendMviEvent` + `updateState`
4. Activity/Fragment 基类自动使用 `lifecycleScope` + `repeatOnLifecycle`，无需手动管理协程生命周期
5. `FlowEventBus.tryPost()` 不再使用 `runBlocking`，改用 `tryEmit`
6. **SimpleMvi* 基类**（2.0 后续版本）：须声明第四类型参数 `VM`，例如 `SimpleMviActivity<VB, S, I, MyViewModel>`，以便正确推断并创建 ViewModel

## ProGuard / R8

本库已通过 `consumer-rules.pro` 自动导出混淆规则，宿主应用无需手动配置。

库内部的泛型推断（`inferViewModelClass`）依赖反射读取泛型签名，`consumer-rules.pro` 已保留必要的 `Signature` 属性和 ViewModel 构造函数。

如宿主项目有特殊需求，可在 `proguard-rules.pro` 中追加规则。

**验证**：本仓库 `demo` 模块的 `release` 构建已启用 `minifyEnabled`，可用于本地验证合并后的 consumer 规则（需 JDK 17）：

```bash
./gradlew :demo:assembleRelease
```

## 线程安全

| 组件 | 线程安全说明 |
|---|---|
| `MviViewModel.updateState` | ✅ 使用 `MutableStateFlow.update`，原子操作 |
| `MviViewModel.dispatch/dispatchThrottled` | ⚠️ 必须在主线程调用 |
| `MviViewModel.sendMviEvent` | ✅ 使用 `Channel.trySend`，线程安全 |
| `FlowEventBus.post/tryPost` | ✅ 内部使用 `CoroutineScope + SharedFlow`，线程安全 |
| `FlowEventBus.observe` | ✅ 返回冷流，线程安全 |
| `AwNav.navigate/back` | ⚠️ 必须在主线程调用（操作 FragmentManager） |
| `BaseViewModel.launch/launchIO/launchDefault` | ✅ 可在任意线程调用 |

## 许可证

Apache License 2.0，详见 [LICENSE](LICENSE)。

# Last updated: 2026年 4月 21日
