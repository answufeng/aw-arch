# aw-arch

[![](https://jitpack.io/v/answufeng/aw-arch.svg)](https://jitpack.io/#answufeng/aw-arch)

基于 **Kotlin + MVVM / MVI / MVP** 的 Android 架构基础库，提供基类、**AwNav** 导航、**FlowEventBus**、**LoadState**、Flow 与生命周期扩展等。

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
    implementation("com.github.answufeng:aw-arch:1.1.0")
}
```

`implementation` 的 **版本号与 Git / JitPack 的 tag 一致**（上例为 `1.1.0`）。

### 2) 打开 ViewBinding（必需）

```kotlin
android {
    buildFeatures { viewBinding = true }
}
```

### 3) 使用 AwNav 时（可选）

库内 `AwNav` 使用 `fragment-ktx` 的 `commit { }` 等 API，但 **`fragment-ktx` 未作为 API 依赖传递**给宿主。若工程里尚未间接引入，请显式添加：

```kotlin
dependencies {
    implementation("androidx.fragment:fragment-ktx:1.8.5") // 版本可按 BOM 对齐
}
```

### 4) 第一个页面（MVVM）

```kotlin
class CounterViewModel : MvvmViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    fun inc() = launch { _count.value++ }
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
}
```

### 5) 第一个页面（MVP）

```kotlin
interface CounterContract {
    interface View : MvpView {
        fun render(count: Int)
    }
}

class CounterPresenter : BaseMvpPresenter<CounterContract.View>() {
    private var count = 0

    fun inc() {
        count++
        viewOrNull?.render(count)
    }
}

class CounterActivity :
    MvpActivity<ActivityCounterBinding, CounterContract.View, CounterPresenter>(),
    CounterContract.View {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { presenter.inc() }
    }

    override fun render(count: Int) {
        binding.tvCount.text = count.toString()
    }
}
```

---

## 目录（按常见需求跳转）

| 想做什么 | 跳转到 |
|----------|--------|
| 最短时间跑通依赖与第一个页面 | [5 分钟上手](#5-分钟上手最小接入) · [环境要求](#环境要求) |
| 避免踩坑 | [集成约定与踩坑](#集成约定与踩坑) |
| 能力列表 / 选型 | [功能概览](#功能概览) · [ViewModel 分层](#viewmodel-分层) |
| 基类体系与选型 | [📖 基类体系](aw-arch/doc/base-classes.md) |
| MVVM 模式 | [📖 MVVM 文档](aw-arch/doc/mvvm.md) |
| MVI 模式 | [📖 MVI 文档](aw-arch/doc/mvi.md) |
| MVP 模式 | [📖 MVP 文档](aw-arch/doc/mvp.md) |
| AwNav 导航 | [📖 AwNav 文档](aw-arch/doc/awnav.md) |
| FlowEventBus 事件总线 | [📖 FlowEventBus 文档](aw-arch/doc/flow-event-bus.md) |
| LoadState 状态管理 | [📖 LoadState 文档](aw-arch/doc/load-state.md) |
| Flow & 生命周期扩展 | [📖 扩展文档](aw-arch/doc/extensions.md) |
| Hilt 集成 | [📖 Hilt 文档](aw-arch/doc/hilt.md) |
| 全局配置 | [📖 AwArch 配置](aw-arch/doc/config.md) |
| Demo 应用结构 | [Demo 应用](#demo-应用) |
| 版本说明 | [CHANGELOG.md](CHANGELOG.md) |
| Demo 与本地构建 | [本仓库与工程检查](#本仓库与工程检查) |
| 混淆 / 线程 | [ProGuard / R8](#proguard--r8) · [线程安全](#线程安全) |
| 协议 | [许可证](#许可证) |

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
| `AwNav` 未在 Activity `onCreate` **先** `init` 就 `navigate` | `error()` 或栈错乱 | Activity 容器：在 `onCreate` 中 `AwNav.init` 后再 `navigate` |
| `AwNav.init(host, …)` 写在 `onCreate`（子容器） | `IllegalStateException` | 须在 Host Fragment 的 **`onViewCreated` 之后**（view 已创建），见 [AwNav 文档](aw-arch/doc/awnav.md) |
| 混淆 `MviEffect` 与 `MvvmViewModel.UiEvent` | import 错、语义混用 | MVI 一次性副作用用 **`MviEffect`**；MVVM 用 `MvvmViewModel.UiEvent`（`mvi.UiEvent` 仅为 deprecated typealias） |
| 在 `Fragment` 用错 `viewLifecycleOwner` 收集流 | 泄漏或重复收集 | 视图相关用 `viewLifecycleOwner`，纯 VM 用 `lifecycleOwner` |
| 基类星投影 `*` 导致 `VM` 泛型被擦除 | `createViewModel()` 推断失败 | 子类写全泛型或覆写 `createViewModel()`（见 `inferViewModelClass` 的 KDoc） |
| `FlowEventBus` 只 `post` 从未订阅却依赖 `autoCleanup` | 与语义不符 | `autoCleanup` 仅在订阅者归零后延迟清理（见 KDoc） |
| `MviViewModel.event` / `MvvmViewModel.uiEvent` 多收集者分摊 | 事件丢失 | Channel + receiveAsFlow 仅单收集者安全；多收集者场景需自行转 SharedFlow 或保证只有一个收集者 |

---

## 功能概览

| 能力 | 说明 | 详细文档 |
|------|------|----------|
| **MVVM** | `MvvmViewModel` + `MvvmActivity/Fragment/Dialog` 等基类 | [📖 MVVM](aw-arch/doc/mvvm.md) |
| **MVI** | `MviViewModel<State, MviEffect, Intent>` + 对应基类 | [📖 MVI](aw-arch/doc/mvi.md) |
| **MVP** | `BaseMvpPresenter` + `MvpActivity/Fragment/Dialog`（Presenter 跨配置变更保留） | [📖 MVP](aw-arch/doc/mvp.md) |
| **SimpleMVI** | 无独立 `MviEffect` 的简化版本 | [📖 MVI](aw-arch/doc/mvi.md) |
| **AwNav** | Fragment 导航：动画、拦截器、Tab 独立栈、子容器、`BackDispatcherChain` | [📖 AwNav](aw-arch/doc/awnav.md) |
| **ArchView** | MVVM / MVP 共用 UI 契约（Toast、Loading、导航） | [📖 基类](aw-arch/doc/base-classes.md) |
| **FlowEventBus** | 基于 SharedFlow：类型安全、粘性事件、自动清理 | [📖 FlowEventBus](aw-arch/doc/flow-event-bus.md) |
| **LoadState** | Loading/Success/Error：`map` / `fold` / `recover` 等 | [📖 LoadState](aw-arch/doc/load-state.md) |
| **Flow 扩展** | `throttleFirst`、`select`、`throttleClicks`、`bindLifecycle` | [📖 扩展](aw-arch/doc/extensions.md) |
| **生命周期扩展** | `collectOnLifecycle` / `bindLifecycle`、`observeEvent(sticky=)` | [📖 扩展](aw-arch/doc/extensions.md) |
| **Hilt 集成** | `HiltMvvm*` / `HiltMvi*` / `HiltMvp*` / `HiltSimpleMvi*` 基类 | [📖 Hilt](aw-arch/doc/hilt.md) |
| **全局配置** | `AwArch.init { ... }` | [📖 配置](aw-arch/doc/config.md) |

---

## 架构选型（XML 传统开发）

| 场景 | 推荐 |
|------|------|
| 大多数新页面 | **MVVM**（`MvvmViewModel` + 可选 `MvvmViewModel.UiEvent`） |
| 单向数据流、无一次性副作用 | **SimpleMVI** |
| Toast/导航等与 State 分离 | **完整 MVI**（`MviEffect` + `handleEvent`） |
| 遗留 MVP / 已有 Contract | **MVP**（Activity / Fragment Presenter 均跨配置变更保留） |
| 单容器页面栈 | **AwNav**（默认左右滑动动画，整页转场） |
| BottomNav 各 Tab 独立栈 | `AwNav.initTabs()` + `switchTab()` |
| BottomNav + overlay + 子栈 | Activity AwNav + `AwNav.init(host)` + `BackDispatcherChain`，见 [AwNav](aw-arch/doc/awnav.md) |

---

## ViewModel 分层

```
BaseViewModel          ← 协程（launch / launchIO / SavedStateHandle 等）
├── MvvmViewModel      ← + UiEvent（Toast / Loading / navigate）
└── MviViewModel       ← + State / MviEffect / Intent
    └── SimpleMviViewModel  ← 简化（无独立 Effect 类型）
```

---

## Demo 应用

本仓库 `demo/` 模块可直接安装运行（应用名 **aw-arch Demo**）。主界面五个入口：

| 入口 | 内容 |
|------|------|
| **架构模式** | MVVM / MVI / SimpleMVI / MVP 完整示例页 |
| **Hilt 依赖注入** | Hilt + MVVM / MVI / SimpleMVI / MVP |
| **LoadState** | Loading / Success / Error 与重试 |
| **FlowEventBus** | 类型安全总线与粘性消息 |
| **AwNav 导航** | 基础路由（多种动画）、拦截器、Tab 独立栈、多 Tab + Overlay（WeChat 综合示例） |

本地运行：`./gradlew :demo:installDebug`（需 JDK 17）。

---

## ProGuard / R8

本库通过 **`consumer-rules.pro`** 导出规则，一般无需宿主额外配置。`inferViewModelClass` 依赖反射签名，规则已保留 `Signature` 与 ViewModel 构造。

`AwNav` 通过类名实例化 Fragment；宿主开启 R8 时请为已注册 Fragment 增加保留规则（按包名收窄）：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

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
| Demo | 模块 `demo/`；结构见上文 [Demo 应用](#demo-应用) |
| 本地建议命令 | `./gradlew :aw-arch:assembleRelease :aw-arch:ktlintCheck :aw-arch:lintRelease :demo:assembleRelease`（需 **JDK 17**） |
| CI | [`.github/workflows/ci.yml`](.github/workflows/ci.yml)：assemble、ktlint、Lint、demo assemble |

---

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

---

*文档更新：2026-06-10*
