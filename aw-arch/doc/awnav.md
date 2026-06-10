# AwNav 导航

轻量级 Fragment 导航控制器，基于 `add + attach/detach` 管理，不依赖 FM BackStack。纯代码实现，无 XML 配置，面向传统 View 体系。

## 核心特性

- 路由注册与 Fragment 实例化（`@AwNavRoute` 注解 / 手动注册 / DSL 批量注册）
- `add + attach/detach` 管理，Fragment 实例不会被重建
- 自维护多 Tab 独立返回栈，Tab 切换不重建页面
- 拦截器（如登录拦截）
- 返回栈管理（back / backTo / clearStack / clearAndNavigate / clearGroup）
- Fragment 级返回拦截（OnGoBackListener）
- Fragment 级管理策略（NavigatorTransaction: ATTACH_DETACH / SHOW_HIDE）
- 内置动画（FADE / SLIDE_HORIZONTAL / SLIDE_VERTICAL）+ Tab 切换动画
- 防连点节流（300ms 窗口）
- 主线程断言（可选）
- 响应式路由观察（currentRouteFlow）
- Fragment 间结果回传（navigateForResult / setFragmentResult）
- 进程重建自动恢复
- 调试栈视图（dumpStack）

## 初始化

### Activity 主容器

在 Activity `onCreate` 中初始化：

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 方式 1：手动注册路由
        nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState)
            .register<HomeFragment>("home")
            .register<ProfileFragment>("profile")
            .register<SettingsFragment>("settings")

        // 方式 2：使用 @AwNavRoute 注解 + registerAnnotated
        nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState)
            .registerAnnotated<HomeFragment>()
            .registerAnnotated<ProfileFragment>()
            .registerAnnotated<SettingsFragment>()

        // 首次导航（自动成为根页面）
        if (savedInstanceState == null) {
            nav.navigate("home")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nav.saveState(outState)
    }
}
```

### @AwNavRoute 注解

在 Fragment 类上标注 `@AwNavRoute` 声明路由名，配合 `registerAnnotated()` 使用：

```kotlin
@AwNavRoute("home")
class HomeFragment : BaseFragment<FragmentHomeBinding>() { ... }

@AwNavRoute("detail")
class DetailFragment : BaseFragment<FragmentDetailBinding>() { ... }

nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState)
    .registerAnnotated<HomeFragment>()
    .registerAnnotated<DetailFragment>()
```

### Fragment 子容器（Tab 内多级 / overlay 内层）

在宿主 Fragment 的 `onViewCreated` 中对子 `FrameLayout` 初始化（使用 childFragmentManager）：

```kotlin
class DetailHostFragment : Fragment(R.layout.fragment_detail_host) {
    private lateinit var nav: AwNav

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nav = AwNav.init(this, R.id.inner_container, savedInstanceState = savedInstanceState)
            .register<DetailFragment>("detail")
        if (savedInstanceState == null) nav.navigate("detail")
    }
}
```

也可继承 `AwNavHostFragment` 自动完成 `init`：

```kotlin
class MyHostFragment : AwNavHostFragment(R.layout.fragment_host, R.id.inner_container) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nav.register {
            route<HomeFragment>("home")
            route<DetailFragment>("detail")
        }
        if (savedInstanceState == null) {
            nav.navigate("home")
        }
    }
}
```

`AwNav.from(fragment)` 会沿 `parentFragment` 链查找最近的子容器实例，再回退到 Activity 级 AwNav。

### DSL 批量注册

```kotlin
nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState).apply {
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
// 基本导航（默认左右滑动动画）
nav.navigate("detail")

// 带参数
nav.navigate("detail", Bundle().apply { putInt("id", 42) })

// 自定义选项
nav.navigate("detail") {
    singleTop = true        // 当前路由相同则复用
    anim = NavAnim.FADE     // 切换为淡入淡出动画
    groupName = "PAYMENT"   // 分组，用于 clearGroup
}
```

### navigateForResult

基于 `FragmentManager.setFragmentResult()` 实现 Fragment 间结果回传，生命周期安全：

```kotlin
// 发起方：导航到地址选择页并监听结果
nav.navigateForResult("address_pick", "req_key") { result ->
    val address = result.getString("address")
}

// 结果方：在被导航的 Fragment 中回传结果
nav.setFragmentResult("req_key", bundleOf("address" to "杭州市"))
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

### back / backTo / clearStack / clearAndNavigate / clearGroup

```kotlin
// 返回上一页
val handled = nav.back()

// 返回到指定路由（inclusive = true 则包含目标路由也弹出）
nav.backTo("home", inclusive = false)

// 清空当前 Tab 栈
nav.clearStack()

// 清空当前 Tab 栈并导航到新页面
nav.clearAndNavigate("home") { anim = NavAnim.FADE }

// 清除指定分组的所有页面
nav.clearGroup("PAYMENT")
```

### singleTop 与 onSingleTopReuse

```kotlin
nav.navigate("detail") {
    singleTop = true
    onSingleTopReuse = { existingFragment ->
        Toast.makeText(this, "singleTop: 复用了现有 Fragment", Toast.LENGTH_SHORT).show()
    }
}
```

## 多 Tab 独立返回栈

使用 `initTabs` + `switchTab` 管理多 Tab，每个 Tab 维护独立返回栈。Tab 切换只做 `detach/attach`，Fragment 不会重建。

### 基本用法

```kotlin
nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState)
    .register<HomeFragment>("home")
    .register<DetailFragment>("detail")
    .register<SettingsFragment>("settings")

nav.initTabs(
    TabConfig("a", rootRoute = "home"),
    TabConfig("b", rootRoute = "home"),
    TabConfig("c", rootRoute = "home"),
)

// 同一个 Fragment 类可以复用同一个路由名，不同 Tab 各自维护独立栈

// Tab 切换
binding.bottomNavigation.setOnItemSelectedListener { item ->
    val tabId = when (item.itemId) {
        R.id.tab_a -> "a"
        R.id.tab_b -> "b"
        R.id.tab_c -> "c"
        else -> return@setOnItemSelectedListener false
    }
    nav.switchTab(tabId)
    true
}

// Tab 内多级仍用普通 navigate（默认左右滑动动画）
nav.navigate("detail")
```

### Tab 切换动画

```kotlin
nav.initTabs(
    TabConfig("wechat", rootRoute = "wechat", switchAnim = NavAnim.FADE),
    TabConfig("contact", rootRoute = "contact"),
    TabConfig("discover", rootRoute = "discover"),
    TabConfig("profile", rootRoute = "profile"),
)
```

### onTabReselect 回调

重复点击当前 Tab 时触发，可用于"回到顶部"：

```kotlin
nav.onTabReselect = { tabId ->
    nav.backTo(nav.currentRoute ?: return@onTabReselect)
}
```

### 进程重建恢复

`AwNav.init()` 接收 `savedInstanceState` 参数，自动从 FragmentManager 恢复栈状态。只需在 `onSaveInstanceState` 中调用 `nav.saveState(outState)`：

```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    nav.saveState(outState)
}
```

## Fragment 级返回拦截

Fragment 实现 `OnGoBackListener` 接口可拦截返回键：

```kotlin
class DetailFragment : BaseFragment<FragmentDetailBinding>(), OnGoBackListener {

    override fun onGoBack(): Boolean {
        // 如果不在顶部，先滚到顶部，拦截返回
        if (binding.scrollView.scrollY > 0) {
            binding.scrollView.smoothScrollTo(0, 0)
            return false // 拦截返回
        }
        return true // 允许返回
    }
}
```

## Fragment 管理策略

默认使用 `attach/detach` 管理 Fragment 可见性（detach 时销毁 View 但保留 Fragment 实例，内存友好）。对于需要保留 View 状态的页面（如 WebView、地图），Fragment 可实现 `OnNavigatorTransactionListener` 指定 `show/hide` 策略：

```kotlin
class WebViewFragment : Fragment(), OnNavigatorTransactionListener {

    override val navigatorTransaction: NavigatorTransaction
        get() = NavigatorTransaction.SHOW_HIDE

    // show/hide 不触发生命周期回调，View 保留在内存
    // 适合 WebView、地图等需要保留复杂 View 状态的页面
}
```

| 策略 | 生命周期 | 内存 | 适用场景 |
|------|---------|------|---------|
| `ATTACH_DETACH`（默认） | detach 触发 onPause→onStop，View 被销毁 | 低 | 大多数页面 |
| `SHOW_HIDE` | 不触发生命周期回调，View 保留 | 高 | WebView、地图等 |

## 拦截器

```kotlin
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
| `NavAnim.SLIDE_HORIZONTAL` | 左右滑动（默认） |
| `NavAnim.FADE` | 淡入淡出 |
| `NavAnim.SLIDE_VERTICAL` | 上下滑动 |
| `NavAnim.NONE` | 无动画 |

默认使用 `SLIDE_HORIZONTAL`（新页面从右滑入，返回时向右滑出），无需显式指定。退出动画自动使用对应 pop 动画（进入时记录，退出时回放）。

## 与 MVVM 集成

在 MVVM 基类中覆写 `awNav`，`UiEvent.Navigate` / `UiEvent.NavigateBack` 自动交给 AwNav：

```kotlin
class MainActivity : MvvmActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var nav: AwNav
    override val awNav: AwNav get() = nav

    override fun onCreate(savedInstanceState: Bundle?) {
        nav = AwNav.init(this, R.id.container, savedInstanceState = savedInstanceState)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
        super.onCreate(savedInstanceState)
    }
}
```

## 状态查询

```kotlin
val route: String? = nav.currentRoute     // 当前路由名
val tabId: String = nav.currentTabId      // 当前 Tab ID
val depth: Int = nav.stackDepth           // 当前 Tab 栈深度
val depthA: Int = nav.stackDepth("a")     // 指定 Tab 栈深度
val canBack: Boolean = nav.canGoBack()    // 当前 Tab 是否可返回

// 响应式观察路由变化
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        nav.currentRouteFlow.collect { route ->
            tvStatus.text = "Tab: ${nav.currentTabId} | Route: ${route ?: "-"} | Depth: ${nav.stackDepth}"
        }
    }
}
```

## 返回键优先级链

当同一 Activity 存在 overlay、AwNav、系统返回等多层逻辑时，可用 `BackDispatcherChain` 按优先级串联：

```kotlin
AwNav.init(this, R.id.container, handleBackPressed = false, savedInstanceState = savedInstanceState)

BackDispatcherChain(onBackPressedDispatcher, this)
    .add(100) { popOverlay() }   // 先处理全屏层
    .add(50) { nav.back() }      // 再 AwNav pop
    .install()
```

未消费时 Chain 会委托 `OnBackPressedDispatcher.onBackPressed()`（如 `finish()`）。

## 调试

```kotlin
Log.d("AwNav", nav.dumpStack())
// 输出示例：
// Tab: wechat (depth=2) *
//   [0] WeChatFragment (route=wechat, ATTACH_DETACH)
//   [1] ChatDetailFragment (route=chat_detail, ATTACH_DETACH)
// Tab: contact (depth=1)
//   [0] ContactFragment (route=contact, ATTACH_DETACH)
// Current: chat_detail | Tab: wechat | Stack depth: 2
```

## 主线程断言

在 `AwArch.init` 中开启：

```kotlin
AwArch.init {
    strictMainThreadForAwNav = BuildConfig.DEBUG
}
```

## 注意事项

- `AwNav.init` 必须在 `navigate` 之前调用，否则抛 `error()`
- `navigate` / `back` / `backTo` / `clearStack` 必须在主线程调用
- 防连点节流为按 route 300ms 窗口，可通过 `NavOptions.disableThrottle` 跳过
- `FragmentManager.isStateSaved` 为 true 时，导航操作会被忽略
- 进程重建恢复：`init()` 传入 `savedInstanceState`，`onSaveInstanceState` 中调用 `nav.saveState(outState)`
- 开启 R8 时，需为已注册的 Fragment 添加保留规则：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

## 迁移指南（旧 API → 新 API）

| 旧 API | 新 API | 说明 |
|---------|--------|------|
| `AwNavTabSwitcher` | `AwNav.initTabs()` + `AwNav.switchTab()` | Tab 管理内置到 AwNav |
| `AwNavTab(id, rootRoute, fragmentCls, subRoutes, ...)` | `TabConfig(id, rootRoute, switchAnim)` | 简化配置 |
| `AwNavTab.fragmentCls` / `subRoutes` | `AwNav.register()` | 路由注册统一用 register |
| `nav.tabSwitcher(listOf(AwNavTab(...)))` | `nav.initTabs(TabConfig(...))` | 初始化方式 |
| `tabSwitcher.selectTab(tabId, args)` | `nav.switchTab(tabId)` | 切换 Tab |
| `tabSwitcher.selectedTabId` | `nav.currentTabId` | 当前 Tab |
| `tabSwitcher.onReselect` | `nav.onTabReselect` | 重复选择回调 |
| `tabSwitcher.saveState/restoreState` | `nav.saveState(outState)` + `init(savedInstanceState)` | 状态保存恢复 |
| `NavOptions.addToBackStack` | 删除 | 首次 navigate 自动成为根页面 |
| `NavOptions.backStackName` | 删除 | 不再使用 FM BackStack |
| `nav.navigate("home") { addToBackStack = false }` | `nav.navigate("home")` | 首次导航无需特殊处理 |
| `nav.stackDepth`（无参） | `nav.stackDepth` | 当前 Tab 栈深度 |
| 路由前缀 `"tab_a_detail"` | 共享路由 `"detail"` | 同一 Fragment 类可复用同一路由名 |

## 能力边界

| 场景 | AwNav 是否原生支持 | 推荐做法 |
|------|-------------------|----------|
| 单容器线性栈 A→B→C | ✅ | 直接使用 `navigate` / `back` |
| BottomNav 切换 Tab（保留各 Tab 返回栈） | ✅ | `initTabs` + `switchTab` |
| Tab 内多级页面 | ✅ | `AwNav.init(hostFragment, containerId)` 或 `AwNavHostFragment` |
| Fragment 子容器内导航 | ✅ | `AwNav.init(host, containerId)` / `AwNav.from(fragment)` |
| Fragment 间结果回传 | ✅ | `navigateForResult` / `setFragmentResult` |
| Fragment 返回键拦截 | ✅ | `OnGoBackListener` |
| 分组清除 | ✅ | `clearGroup(groupName)` |
| WebView/地图保活 | ✅ | `OnNavigatorTransactionListener` → `SHOW_HIDE` |
| ViewPager 多页保活 | ❌ | `FragmentStateAdapter`，不用 AwNav |
| 返回键：overlay 内层 → 关 overlay → AwNav pop | ✅ | `BackDispatcherChain` |

结构示意：

```
Activity.supportFragmentManager
├── container          ← AwNav：Tab 根页面 + 子页面
├── overlay（可选）    ← 手写：全屏流程 Host
│   └── inner_container ← AwNav.init(overlayHost)：2/3 级页
└── BottomNav          ← Activity 逻辑
```
