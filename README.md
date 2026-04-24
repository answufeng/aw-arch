# aw-arch

[![](https://jitpack.io/v/answufeng/aw-arch.svg)](https://jitpack.io/#answufeng/aw-arch)

基于 **Kotlin + MVVM / MVI** 的 Android 架构基础库，提供基类、**AwNav** 导航、**FlowEventBus**、**LoadState**、Flow 与生命周期扩展、ViewBinding 委托等。

如果你只想最快接入并跑通第一个页面，直接看下面的「5 分钟上手」即可；其它内容都可以后置按需查阅。

---

## 5 分钟上手（最小接入）

### 1) 添加依赖（JitPack）

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

`implementation` 的 **版本号与 Git / JitPack 的 tag 一致**（上例为 `1.0.0`）。

### 2) 打开 ViewBinding（必需）

```kotlin
android {
    buildFeatures { viewBinding = true }
}
```

### 3) 第一个页面（MVVM）

```kotlin
class CounterViewModel : MvvmViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    fun inc() = launch { _count.value++ }
}

class CounterActivity : MvvmActivity<ActivityCounterBinding, CounterViewModel>() {
    override fun viewModelClass() = CounterViewModel::class.java
    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { viewModel.inc() }
    }
    override fun initObservers() {
        super.initObservers()
        viewModel.count.collectOnLifecycle(this) { binding.tvCount.text = it.toString() }
    }
}
```

---

## 目录（按常见需求跳转）

| 想做什么 | 跳转到 |
|----------|--------|
| 最短时间跑通依赖与第一个页面 | [5 分钟上手（最小接入）](#5-分钟上手最小接入) · [环境要求](#环境要求) |
| 避免踩坑（导航 / 事件总线 / 生命周期） | [集成约定与踩坑](#集成约定与踩坑) |
| 能力列表 / 选型（MVVM / MVI） | [功能概览](#功能概览) · [ViewModel 分层](#viewmodel-分层) |
| 按模块看 API：AwNav / EventBus / LoadState | [AwNav](#awnav-导航) · [FlowEventBus](#floweventbus-事件总线) · [LoadState](#loadstate-状态管理) |
| Demo 与本地构建 | [本仓库与工程检查](#本仓库与工程检查) |
| 混淆 / 迁移 / 线程 | [ProGuard / R8](#proguard--r8) · [迁移指南](#迁移指南1x--20) · [线程安全](#线程安全) |
| 协议 | [许可证](#许可证) |

演示场景与手测建议：**[demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md)**

---

## 环境要求

| 项目 | 最低版本 |
|------|----------|
| Kotlin | 2.x（以仓库 Gradle 版本为准） |
| Android minSdk | 24 |
| Android compileSdk / targetSdk（demo） | 35 |
| JDK | 17 |
| AGP | 8.2.2+ |
| Gradle | 8.11+ |

---

## 集成约定与踩坑

| 误用 | 后果 | 正确做法 |
|------|------|----------|
| `AwNav` 未在 Activity `onCreate` **先** `init` 就 `navigate` | `error()` 或栈错乱 | 保证 `AwNav.init` 与 `FragmentContainerView` 生命周期一致 |
| 在 `Fragment` 用错 `viewLifecycleOwner` 收集流 | 泄漏或重复收集 | 视图相关用 `viewLifecycleOwner`，纯 VM 用 `lifecycleOwner` |
| 基类星投影 `*` 导致 `VM` 泛型被擦除 | `createViewModel()` 推断失败 | 子类写全泛型或覆写 `createViewModel()`（见 `inferViewModelClass` 的 KDoc） |
| `FlowEventBus` 只 `post` 从未订阅却依赖 `autoCleanup` | 与语义不符 | `autoCleanup` 仅在订阅者归零后延迟清理（见 KDoc） |

---

## 功能概览

| 能力 | 说明 |
|------|------|
| **MVVM** | `MvvmViewModel` + `MvvmActivity/Fragment/Dialog` 等基类 |
| **MVI** | `MviViewModel<State, Event, Intent>` + 对应基类 |
| **SimpleMVI** | 无自定义 Event 的简化版本（泛型含 VM） |
| **AwNav** | 纯代码 Fragment 导航：动画、拦截器、回退栈、防连点 |
| **FlowEventBus** | 基于 SharedFlow：类型安全、粘性事件、自动清理 |
| **LoadState** | Loading/Success/Error：`map` / `fold` / `recover` 等 |
| **Flow 扩展** | `throttleFirst`、`debounceAction`、`select`、`throttleClicks` |
| **生命周期扩展** | `collectOnLifecycle`、`observeEvent`、`launchOnStarted/Resumed` |
| **ViewBinding 委托** | Activity/Fragment 属性委托，零反射 |

---

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
├── MvvmViewModel      ← + UiEvent（Toast / Loading / navigate）
└── MviViewModel       ← + State / Event / Intent
    └── SimpleMviViewModel  ← 简化（NoEvent）
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
            .addInterceptor { _, to, _ -> to != "profile" || userManager.isLoggedIn }

        if (savedInstanceState == null) nav.navigate("home") { addToBackStack = false; anim = NavAnim.NONE }
    }
}
```

Fragment 内导航：

```kotlin
AwNav.from(this).navigate("settings")
```

---

## FlowEventBus 事件总线

```kotlin
FlowEventBus.post(LoginSuccessEvent("user123"))
FlowEventBus.postSticky(ThemeChangedEvent(darkMode = true))

FlowEventBus.observe<LoginSuccessEvent>()
    .collectOnLifecycle(this) { updateUI(it.userId) }
```

`autoCleanupDelay` 默认 30s：仅当某类型**曾经有过订阅者**且订阅数归零超过该时间后才清理对应通道；普通与粘性通道分别清理。

---

## LoadState 状态管理

```kotlin
data class HomeState(val items: LoadState<List<String>> = LoadState.Loading) : UiState

val result = loadStateCatching { repository.fetchItems() }
val timeout = loadStateWithTimeout(5_000) { repository.fetchItems() }
```

---

## ProGuard / R8

本库通过 **`consumer-rules.pro`** 导出规则，一般无需宿主额外配置。`inferViewModelClass` 依赖反射签名，规则已保留 `Signature` 与 ViewModel 构造。

`AwNav` 通过类名实例化 Fragment；宿主开启 R8 时请为已注册 Fragment 增加保留规则（按包名收窄）：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

---

## 迁移指南（1.x → 2.0）

1. `BaseViewModel` → `MvvmViewModel`（MVVM 场景）  
2. `BaseViewModel.UiEvent` → `MvvmViewModel.UiEvent`  
3. `MviViewModel` 不再内置 `showToast` / `showLoading` / `navigate`，改用 `sendMviEvent` + `updateState`  
4. 基类已接 `lifecycleScope` + `repeatOnLifecycle`  
5. `FlowEventBus.tryPost()` 使用 `tryEmit`，非 `runBlocking`  
6. **SimpleMvi***：需第四类型参数 `VM`，如 `SimpleMviActivity<VB, S, I, MyViewModel>`  

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

## 本仓库与工程检查

| 项 | 说明 |
|----|------|
| Demo | 模块 `demo/`；入口与手测建议见 [demo/DEMO_MATRIX.md](demo/DEMO_MATRIX.md) |
| 本地建议命令 | `./gradlew :aw-arch:assembleRelease :aw-arch:ktlintCheck :aw-arch:lintRelease :demo:assembleRelease`（需 **JDK 17**） |
| CI | [`.github/workflows/ci.yml`](.github/workflows/ci.yml)：assemble、ktlint、Lint、demo assemble |

---

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

---

*文档更新：2026-04-24*
