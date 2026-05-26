# AwNav 导航

轻量级 Fragment 导航控制器，替代 Navigation Component 的简化方案。纯代码实现，无 XML 配置，面向传统 View 体系。

## 核心特性

- 路由注册与 Fragment 实例化
- 拦截器（如登录拦截）
- 返回栈管理（back / backTo / clearStack）
- DSL 批量注册
- 内置动画（FADE / SLIDE_HORIZONTAL / SLIDE_VERTICAL）
- 防连点节流（300ms 窗口）
- 主线程断言（可选）

## 初始化

### Activity 主容器

在 Activity `onCreate` 中初始化：

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav = AwNav.init(this, R.id.container)
            .register<HomeFragment>("home")
            .register<ProfileFragment>("profile")
            .register<SettingsFragment>("settings")

        if (savedInstanceState == null) {
            nav.navigate("home") {
                addToBackStack = false
                anim = NavAnim.NONE
            }
        }
    }
}
```

### Fragment 子容器（Tab 内多级 / overlay 内层）

在宿主 Fragment 的 `onViewCreated` 中对子 `FrameLayout` 初始化（使用 [childFragmentManager]）：

```kotlin
class DetailHostFragment : Fragment(R.layout.fragment_detail_host) {
  private lateinit var nav: AwNav
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    nav = AwNav.init(this, R.id.inner_container)
      .register<DetailFragment>("detail")
    if (savedInstanceState == null) nav.navigate("detail")
  }
}
```

也可继承 [`AwNavHostFragment`](../../src/main/java/com/answufeng/arch/nav/AwNavHostFragment.kt) 自动完成 `init`。

[`AwNav.from(fragment)`](../../src/main/java/com/answufeng/arch/nav/AwNav.kt) 会沿 `parentFragment` 链查找最近的子容器实例，再回退到 Activity 级 AwNav。

### DSL 批量注册

```kotlin
nav = AwNav.init(this, R.id.container).apply {
    register {
        route<HomeFragment>("home")
        route<ProfileFragment>("profile")
        route<SettingsFragment>("settings")
    }
    addInterceptor { _, to, _ -> to != "profile" || userManager.isLoggedIn }
}
```

## 导航操作

### navigate

```kotlin
// 基本导航
nav.navigate("detail")

// 带参数
nav.navigate("detail", Bundle().apply { putInt("id", 42) })

// 自定义选项
nav.navigate("detail") {
    addToBackStack = true   // 默认 true
    singleTop = true        // 当前路由相同则跳过
    anim = NavAnim.FADE     // 内置动画
}
```

### 自定义动画

```kotlin
nav.navigate("detail") {
    setCustomAnim(
        enter = R.anim.slide_in_right,
        exit = R.anim.slide_out_left,
        popEnter = R.anim.slide_in_left,
        popExit = R.anim.slide_out_right,
    )
}
```

### back / backTo / clearStack

```kotlin
// 返回上一页
val handled = nav.back()

// 返回到指定路由（inclusive = true 则包含目标路由也弹出）
nav.backTo("home", inclusive = false)

// 清空整个返回栈（同时移除容器内当前 Fragment，并重置 currentRoute）
nav.clearStack()
```

### singleTop 与 clearStack 边界

- **singleTop**：仅当容器内已存在 tag 为 route 的 Fragment 时复用并返回；若 `_currentRoute` 仍为该 route 但 Fragment 已被 `clearStack` 等移除，会继续正常 `navigate`，避免静默跳过。
- **clearStack**：弹出全部返回栈条目，并 `remove` 容器内当前 Fragment，随后 `syncCurrentRoute()`；清空后 `currentRoute` 一般为 `null`。

### Fragment 子容器 init 时机

`AwNav.init(host, containerId)` 在 `handleBackPressed = true`（默认）时要求 **host 的 view 已创建**（例如在 `onViewCreated` 中调用），否则会抛出 `IllegalStateException`。与 `AwNavHostFragment` 用法一致。

## Fragment 内导航

```kotlin
class HomeFragment : Fragment() {
    private fun openSettings() {
        AwNav.from(this).navigate("settings")
    }
}
```

**注意：** `AwNav.from(fragment)` 沿 `parentFragment` 链查找：若某层 Host 已 `AwNav.init(host, containerId)`，则返回该 **子容器** 实例；否则回退到 Activity 级 AwNav。子容器用法见上文 **Fragment 子容器** 与 `AwNavHostFragment`。

## 拦截器

```kotlin
// 登录拦截
nav.addInterceptor { from, to, args ->
    if (to == "profile" && !userManager.isLoggedIn) {
        AwNav.from(activity).navigate("login")
        false  // 拦截本次导航
    } else {
        true   // 允许导航
    }
}
```

## NavAnim 内置动画

| 动画 | 说明 |
|------|------|
| `NavAnim.NONE` | 无动画 |
| `NavAnim.FADE` | 淡入淡出 |
| `NavAnim.SLIDE_HORIZONTAL` | 左右滑动（默认） |
| `NavAnim.SLIDE_VERTICAL` | 上下滑动 |

## 与 MVVM 集成

在 MVVM 基类中覆写 `awNav`，`UiEvent.Navigate` / `UiEvent.NavigateBack` 自动交给 AwNav：

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

## 状态查询

```kotlin
val route: String? = nav.currentRoute   // 当前路由名
val depth: Int = nav.stackDepth         // 返回栈深度
```

## 主线程断言

在 `AwArch.init` 中开启：

```kotlin
AwArch.init {
    strictMainThreadForAwNav = BuildConfig.DEBUG
}
```

开启后，非主线程调用 `navigate` / `back` / `backTo` / `clearStack` 会抛出 `IllegalStateException`。

## 注意事项

- `AwNav.init` 必须在 `navigate` 之前调用，否则抛 `error()`
- `navigate` / `back` / `backTo` / `clearStack` 必须在主线程调用
- 防连点节流为 **按 route** 300ms 窗口，可通过 `NavOptions.disableThrottle` 跳过；`AwArch.logAwNavThrottledNavigations` 可记录被忽略的导航
- `FragmentManager.isStateSaved` 为 true 时，导航操作会被忽略
- 开启 R8 时，需为已注册的 Fragment 添加保留规则：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

## 多 Tab 独立返回栈

基于 AndroidX `FragmentManager.saveBackStack` / `restoreBackStack`（要求 `addToBackStack` 使用命名栈，且事务 `setReorderingAllowed(true)`——AwNav 已默认开启）。

```kotlin
val nav = AwNav.init(this, R.id.container)
    .register<HomeFragment>("tab_a_home")
    .register<DetailFragment>("tab_a_detail")
    // … 为每个 Tab 注册 home / detail 路由

val tabSwitcher = nav.tabSwitcher(
    listOf(
        AwNavTab(id = "a", rootRoute = "tab_a_home"),
        AwNavTab(id = "b", rootRoute = "tab_b_home"),
    ),
)

// 切换 Tab（保留各自返回栈）
tabSwitcher.selectTab("b")

// Tab 内多级仍用普通 navigate
nav.navigate("tab_a_detail")
```

Tab 根页面首次进入由 `selectTab` 负责，内部使用 `NavOptions.backStackName = tab.backStackName`。

进程重建时在 Activity `onCreate` 中调用 `tabSwitcher.restoreState(savedInstanceState)`，并在 `onSaveInstanceState` 中 `tabSwitcher.saveState(outState)`（FragmentManager 会自行恢复各 Tab 栈）。

参见 Demo：`AwNavTabStackDemoActivity`；微信 Demo 的 BottomNav 亦改用 `AwNavTabSwitcher`。

## 返回键优先级链

当同一 Activity 存在 overlay、AwNav、系统返回等多层逻辑时，可用 `BackDispatcherChain` 按优先级串联：

```kotlin
AwNav.init(this, R.id.container, handleBackPressed = false) // 由 Chain 统一处理

BackDispatcherChain(onBackPressedDispatcher, this)
    .add(100) { popOverlay() }   // 先处理全屏层
    .add(50) { nav.back() }      // 再 AwNav pop
    .install()
```

未消费时 Chain 会委托 `OnBackPressedDispatcher.onBackPressed()`（如 `finish()`）。

## 高级用法：Tab + 全屏叠加

参见 Demo 中 `WeChatActivity`，演示了：
- Tab 内使用 `AwNavTabSwitcher` 保留各 Tab 返回栈
- 全屏 overlay 叠加层（不隐藏 Tab/底栏）
- 内层 child 栈与 AwNav 返回栈隔离（详情页不走 AwNav 加深 Activity 栈）

## 能力边界（单 Activity 多 Fragment）

| 场景 | AwNav 是否原生支持 | 推荐做法 |
|------|-------------------|----------|
| 单容器线性栈 A→B→C | ✅ | 直接使用 `navigate` / `back` |
| BottomNav 切换 Tab（不保留各 Tab 返回栈） | ⚠️ | `clearStack()` + `navigate(tab) { addToBackStack = false }` |
| 每 Tab 独立返回栈 | ✅ | [AwNavTabSwitcher](#多-tab-独立返回栈) + `NavOptions.backStackName` |
| Tab 内多级页面 | ✅ | `AwNav.init(hostFragment, containerId)` 或 `AwNavHostFragment`（WeChat overlay 内层） |
| Fragment 子容器内导航 | ✅ | `AwNav.init(host, containerId)` / `AwNav.from(fragment)` |
| ViewPager 多页保活 | ❌ | `FragmentStateAdapter`，不用 AwNav replace |
| 返回键：overlay 内层 → 关 overlay → AwNav pop | ✅ | [BackDispatcherChain](#返回键优先级链)（WeChat Demo） |

`NavOptions` 补充：

- `singleTop = true`：同 route 时复用实例并更新 `arguments`（若传入 `args`）
- `disableThrottle = true`：跳过该 route 的 300ms 防连点（节流按 **route** 计，不同 route 互不影响）

结构示意：

```
Activity.supportFragmentManager
├── container          ← AwNav：Tab 根页面
├── overlay（可选）    ← 手写：全屏流程 Host
│   └── inner_container ← AwNav.init(overlayHost)：2/3 级页
└── BottomNav          ← Activity 逻辑
```
