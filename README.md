# aw-arch

[![JitPack](https://jitpack.io/v/answufeng/aw-arch.svg)](https://jitpack.io/#answufeng/aw-arch)

Android 架构基础库：**Kotlin + MVVM / MVI + Hilt**，提供基类、**AwNav** 导航、**FlowEventBus**、**LoadState**、Flow 与生命周期扩展、ViewBinding 委托等。

**运行环境**：JDK **17**；库与 **demo** 使用 compileSdk / targetSdk **35**（ViewBinding；demo 含 Hilt）。

---

## 目录

| 想做什么 | 跳转到 |
|----------|--------|
| 引入依赖 | [安装](#安装) |
| 本地 / CI 检查 | [质量与 CI](#质量与-ci) |
| 避免踩坑 | [使用前必读](#使用前必读常见误用) |
| 最快跑通 | [五分钟上手](#五分钟上手最小可复制) |
| 能力清单 | [核心能力一览](#核心能力一览) |
| 演示 App | [Demo](#demo) |
| 分步教程 | [快速开始](#快速开始) |
| 模式与 API | [ViewModel 分层](#viewmodel-分层) · [MVVM](#mvvm-模式) · [MVI](#mvi-模式) · [Hilt](#hilt-集成) · [AwNav](#awnav-导航) · [FlowEventBus](#floweventbus-事件总线) |
| 状态 / Flow / 生命周期 | [LoadState](#loadstate-状态管理) · [Flow 扩展](#flow-扩展) · [生命周期扩展](#生命周期扩展) |
| 其他 | [懒加载](#懒加载) · [BaseActivity](#baseactivity) · [ViewBinding 委托](#viewbinding-委托) · [架构图](#架构图) · [API 速览](#api-速览) |
| 升级 / 混淆 / 线程 | [迁移指南](#迁移指南1x--20) · [ProGuard / R8](#proguard--r8) · [线程安全](#线程安全) |
| 协议 | [许可证](#许可证) |

演示场景与手测建议：**[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)**

---

## 安装

**JitPack**（版本号与 [Release 标签](https://github.com/answufeng/aw-arch/tags)一致，例如首个版本 **`1.0.0`**）：

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.answufeng:aw-arch:1.0.0")
}
```

使用 **Hilt** 时还需：

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

---

## 质量与 CI

| 项目 | 说明 |
|------|------|
| **CI** | [`.github/workflows/ci.yml`](.github/workflows/ci.yml) — JDK 17 下 `:aw-arch:assembleRelease`、`ktlintCheck`、`lintRelease`、`:demo:assembleRelease` |
| **本地（仓库根目录）** | `./gradlew :aw-arch:assembleRelease :aw-arch:ktlintCheck :aw-arch:lintRelease :demo:assembleRelease` |

---

## 使用前必读（常见误用）

| 误用 | 后果 | 正确做法 |
|------|------|----------|
| `AwNav` 未在 Activity `onCreate` **先** `init` 就 `navigate` | `error()` 或栈错乱 | 保证 `AwNav.init` 与容器 `FragmentContainerView` 生命周期一致 |
| `FlowEventBus` 只 `post` 从未 `observe` 却依赖 autoCleanup | 与语义不符，可能占通道 | 阅读 KDoc：`autoCleanup` 在订阅者归零后延迟清理 |
| 在 `Fragment` 用错 `viewLifecycleOwner` 收集流 | 泄漏或重复收集 | 视图相关用 `viewLifecycleOwner`，纯 VM 用 `lifecycleOwner` |
| 基类星投影 `*` 导致 `VM` 泛型被擦除 | `createViewModel()` 推断失败 | 子类写全泛型或覆写 `createViewModel()`，见 `inferViewModelClass` KDoc |

---

## 五分钟上手（最小可复制）

1. **依赖**：`implementation("com.github.answufeng:aw-arch:1.0.0")`；Hilt 见上文；模块启用 `viewBinding = true`。
2. **Application**：`@HiltAndroidApp class App : Application()`；`onCreate` 中可选 `AwArch.init { … }`。
3. **界面**：继承 `HiltMvvmActivity<YourVm, YourBinding>(R.layout.xxx)`（或 `MvvmActivity` / `MviActivity`），在 `onBindViewModel` / `initObservers` 里订阅状态；布局根节点用常规 `ConstraintLayout` + `FragmentContainerView` 即可。

更完整的示例见下文 **[快速开始](#快速开始)** 与 **demo** 工程。

---

## 核心能力一览

| 模块 | 说明 |
|------|------|
| **MVVM** | BaseViewModel + MvvmViewModel + MvvmActivity/Fragment/DialogFragment/BottomSheetDialogFragment |
| **MVI** | MviViewModel + Mvi* 基类 |
| **SimpleMVI** | SimpleMviViewModel + SimpleMvi*（4 个泛型含 VM，无需独立 Event 类型） |
| **Hilt** | HiltMvvm/Mvi* 基类，`abstract val viewModel` 注入 |
| **AwNav** | 纯代码 Fragment 导航：动画、拦截器、回退栈、防连点 |
| **FlowEventBus** | SharedFlow 事件总线，粘性事件、类型安全观察 |
| **LoadState** | Loading/Success/Error，`map` / `fold` / `recover` / `combine` 等 |
| **Flow 扩展** | `throttleFirst`、`debounceAction`、`select`、`throttleClicks` |
| **生命周期** | `collectOnLifecycle`、`observeEvent`、`launchOnStarted` / `Resumed` 等 |
| **ViewBinding** | Activity/Fragment 属性委托，零反射 |

---

## Demo

仓库 **`demo/`** 覆盖 MVVM、MVI、`FlowEventBus`、`AwNav`、Hilt 等入口；索引与推荐手测见 **[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)**。demo 主页「演示清单」可快速跳转各 Activity。

---

## 快速开始

### Step 1：Application

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AwArch.init {
            logger = object : AwLogger {
                override fun d(tag: String, message: String) = Log.d(tag, message)
                override fun w(tag: String, message: String, throwable: Throwable?) = Log.w(tag, message, throwable)
                override fun e(tag: String, message: String, throwable: Throwable?) = Log.e(tag, message, throwable)
            }
            // 可选：debuggable 包下强制 AwNav 主线程、导航节流日志等
            // val d = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            // strictMainThreadForAwNav = d
            // logAwNavThrottledNavigations = d
            // flowEventBusAutoCleanupDelayMs = 60_000L
        }
    }
}
```

### Step 2：ViewModel

```kotlin
class CounterViewModel : MvvmViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() = launch { _count.value++ }
    fun decrement() = launch { _count.value-- }
}
```

### Step 3：Activity + 布局

<details>
<summary><b>展开</b>：示例布局 <code>activity_counter.xml</code> 与 <code>CounterActivity</code></summary>

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

</details>

以上为完整 MVVM 单页。MVI、Hilt、导航等见后续章节。

---

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
├── MvvmViewModel      ← + UiEvent（Toast / Loading / navigate）
└── MviViewModel       ← + State / Event / Intent
    └── SimpleMviViewModel  ← 简化（NoEvent）
```

---

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

### 与 AwNav 联动（单 Activity + 多 Fragment）

宿主中先 `AwNav.init`，再覆写 `awNav`，则 `viewModel.navigate` / `navigateBack` 可走 AwNav：

```kotlin
class MainActivity : MvvmActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        nav = AwNav.init(this, R.id.container).register<HomeFragment>("home")
        super.onCreate(savedInstanceState)
    }

    override val awNav: AwNav get() = nav

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityMainBinding.inflate(inflater)
}
```

Fragment 中可使用 `override val awNav get() = AwNav.from(this)`。若使用 Jetpack Navigation 等并自行实现 `navigateTo`，保持 `awNav` 默认 `null` 即可。

---

## MVI 模式

### State / Event / Intent

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

### 节流分发

```kotlin
binding.btnSubmit.setOnClickListener {
    dispatchThrottled(SubmitIntent.Click, windowMillis = 500)
}
```

### SimpleMVI（无自定义 Event）

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

---

## Hilt 集成

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

---

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

### 从 Fragment 导航

```kotlin
AwNav.from(this).navigate("settings")
```

---

## FlowEventBus 事件总线

```kotlin
FlowEventBus.post(LoginSuccessEvent("user123"))
FlowEventBus.tryPost(LoginSuccessEvent("user123"))
FlowEventBus.postSticky(ThemeChangedEvent(darkMode = true))
FlowEventBus.tryPostSticky(ThemeChangedEvent(darkMode = true))

FlowEventBus.observe<LoginSuccessEvent>().collectOnLifecycle(this) { event ->
    updateUI(event.userId)
}

observeEvent<LoginSuccessEvent> { event ->
    updateUI(event.userId)
}

FlowEventBus.removeSticky<ThemeChangedEvent>()
```

**自动清理**：`FlowEventBus.autoCleanupDelay` 默认 30s；仅当某类型**曾经有过订阅者**且当前订阅数归零超过该时间后才释放对应 `SharedFlow`。只取得 Flow、尚未 `collect` 时**不会**被清理。普通与粘性通道**分别**清理。`0` 或负数可关闭自动清理。

---

## LoadState 状态管理

```kotlin
data class HomeState(
    val items: LoadState<List<String>> = LoadState.Loading
) : UiState

updateState { copy(items = LoadState.Loading) }
val result = loadStateCatching { repository.fetchItems() }
val resultTimeout = loadStateWithTimeout(5_000) { repository.fetchItems() }
updateState { copy(items = result) }

when (val items = state.items) {
    is LoadState.Loading -> showProgressBar()
    is LoadState.Success -> adapter.submitList(items.data)
    is LoadState.Error -> showError(items.message)
}

result.map { it.map { item -> item.name } }
result.getOrNull()
result.getOrDefault(emptyList())
result.recover(emptyList())
result.recoverWith { emptyList() }
result.fold(onLoading = {}, onSuccess = {}, onError = {})

viewModel.dataFlow.asLoadState()
viewModel.loadStateFlow.mapLoadState { it.items }
```

---

## Flow 扩展

```kotlin
viewModel.state.throttleFirst(500).collectOnLifecycle(this) { render(it) }

searchFlow.debounceAction(300).collectOnLifecycle(this) { query -> viewModel.search(query) }

viewModel.state.select { it.count }.collectOnLifecycle(this) { count ->
    binding.tvCount.text = count.toString()
}

binding.btnSubmit.throttleClicks(1000).collectOnLifecycle(this) { viewModel.submit() }
```

---

## 生命周期扩展

```kotlin
observeEvent<LoginSuccessEvent> { updateUI(it.userId) }
observeStickyEvent<ThemeChangedEvent> { applyTheme(it.darkMode) }

launchOnStarted { viewModel.state.collect { render(it) } }
launchOnResumed { refreshData() }
```

---

## 懒加载

`MviFragment` / `MvvmFragment` / `BaseFragment` 支持首次可见时一次性初始化：

```kotlin
class HomeFragment : MviFragment<...>() {
    override fun onLazyLoad() {
        dispatch(HomeIntent.LoadData)
    }
}
```

- 仅在 Fragment 首次 `onResume` 触发  
- 配合 ViewPager 时通常仅当前页触发  
- 进程重启后状态会恢复  

---

## BaseActivity

不含 ViewModel 的纯 ViewBinding 基类：

```kotlin
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateBinding() = ActivitySplashBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener { navigateToMain() }
    }
}
```

---

## ViewBinding 委托

```kotlin
class HomeActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::inflate)
}
```

---

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                           UI Layer                              │
│  MvvmActivity/Fragment/Dialog   MviActivity/Fragment/Dialog      │
│  SimpleMviActivity              HiltMvvm/MviActivity/Fragment   │
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

---

## API 速览

### BaseViewModel

| 方法 | 说明 |
|------|------|
| `launch(onError?, block)` | 主协程，自动异常处理 |
| `launchIO` / `launchDefault` | 调度器封装 |
| `withMain(block)` | 切主线程 |
| `handleException(throwable)` | 可覆写 |

### MvvmViewModel

| 方法 | 说明 |
|------|------|
| `sendEvent` / `showToast` / `showLoading` | UI 事件与反馈 |
| `navigate` / `navigateBack` | 导航（可接 AwNav） |

### MviViewModel

| 方法 | 说明 |
|------|------|
| `dispatch` / `dispatchThrottled` | Intent（主线程） |
| `updateState` / `sendMviEvent` | 状态与一次性事件 |
| `clearThrottleCache` | 清节流缓存 |

### AwNav

| 方法 | 说明 |
|------|------|
| `init` / `register` / `addInterceptor` | 初始化与路由 |
| `navigate` / `back` / `backTo` / `clearStack` | 导航控制 |
| `from(fragment/activity)` | 取实例 |

### FlowEventBus

| 方法 | 说明 |
|------|------|
| `post` / `tryPost` / `postSticky` / `tryPostSticky` | 发送 |
| `observe` / `observeSticky` | 订阅 |
| `removeSticky` / `clear` / `clearAll` | 清理 |

---

## 迁移指南（1.x → 2.0）

1. `BaseViewModel` → `MvvmViewModel`（MVVM 场景）  
2. `BaseViewModel.UiEvent` → `MvvmViewModel.UiEvent`  
3. `MviViewModel` 不再内置 `showToast` / `showLoading` / `navigate`，改用 `sendMviEvent` + `updateState`  
4. 基类已接 `lifecycleScope` + `repeatOnLifecycle`  
5. `FlowEventBus.tryPost()` 使用 `tryEmit`，非 `runBlocking`  
6. **SimpleMvi***：需第四类型参数 `VM`，如 `SimpleMviActivity<VB, S, I, MyViewModel>`  

---

## ProGuard / R8

本库通过 **`consumer-rules.pro`** 导出规则，一般无需宿主额外配置。`inferViewModelClass` 依赖反射签名，规则已保留 `Signature` 与 ViewModel 构造。

**AwNav** 注册的 `Fragment` 依赖类名实例化；宿主开启 R8 时请为已注册 Fragment 增加保留规则，例如：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

（按包名收窄。）

**验证**：`demo` 的 `release` 已启用 `minifyEnabled`：

```bash
./gradlew :demo:assembleRelease
```

（需 JDK 17。）

---

## 线程安全

| 组件 | 说明 |
|------|------|
| `MviViewModel.updateState` | `MutableStateFlow.update`，原子 |
| `dispatch` / `dispatchThrottled` | 须在主线程 |
| `sendMviEvent` / `sendEvent` | `Channel.trySend`，线程安全 |
| `FlowEventBus` post / observe | 线程安全 |
| `AwNav.navigate` / `back` | 须在主线程；可在 `AwArch.init { strictMainThreadForAwNav = true }` 下强制断言 |
| `BaseViewModel.launch*` | 任意线程可调用 |

---

## 许可证

[Apache License 2.0](LICENSE)

---

*文档更新：2026-04-23*
