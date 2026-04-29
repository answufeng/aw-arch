# AwArch 全局配置

aw-arch 的全局配置入口，在 `Application.onCreate()` 中初始化。

## 初始化

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AwArch.init {
            logger = TimberAwLogger()
            strictMainThreadForAwNav = BuildConfig.DEBUG
            logAwNavThrottledNavigations = BuildConfig.DEBUG
            flowEventBusAutoCleanupDelayMs = 60_000L
        }
    }
}
```

## 配置项

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `logger` | `AwLogger` | `DefaultAwLogger()` | 全局日志实现 |
| `strictMainThreadForAwNav` | `Boolean` | `false` | AwNav 操作是否强制主线程断言 |
| `logAwNavThrottledNavigations` | `Boolean` | `false` | 是否记录被节流忽略的导航日志 |
| `flowEventBusAutoCleanupDelayMs` | `Long?` | `null` | FlowEventBus 自动清理延迟（毫秒） |

## AwLogger 接口

```kotlin
interface AwLogger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```

### DefaultAwLogger

默认实现，Android 环境使用 `android.util.Log`，非 Android 环境降级为 `System.out`。

### 自定义 Logger（Timber 示例）

```kotlin
class TimberAwLogger : AwLogger {
    override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
    override fun w(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).w(throwable, message)
    override fun e(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).e(throwable, message)
}
```

## 注意事项

- `AwArch.init` 只需调用一次，建议在 `Application.onCreate()` 中
- `flowEventBusAutoCleanupDelayMs` 设为 `null` 时不修改 FlowEventBus 默认值（30秒）
- `strictMainThreadForAwNav` 建议在 Debug 构建中开启，Release 中关闭
- 所有配置属性均为 `@Volatile`，线程安全
