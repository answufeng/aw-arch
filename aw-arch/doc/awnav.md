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

// 清空整个返回栈
nav.clearStack()
```

## Fragment 内导航

```kotlin
class HomeFragment : Fragment() {
    private fun openSettings() {
        AwNav.from(this).navigate("settings")
    }
}
```

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
- 防连点节流窗口为 300ms，可通过 `AwArch.logAwNavThrottledNavigations` 记录被节流的导航
- `FragmentManager.isStateSaved` 为 true 时，导航操作会被忽略
- 开启 R8 时，需为已注册的 Fragment 添加保留规则：

```proguard
-keep public class com.example.app.**Fragment extends androidx.fragment.app.Fragment { *; }
```

## 高级用法：Tab + 全屏叠加

参见 Demo 中 `WeChatActivity`，演示了：
- Tab 内使用 AwNav 切换 Fragment
- 全屏 overlay 叠加层（不隐藏 Tab/底栏）
- 内层 child 栈与 AwNav 返回栈隔离
