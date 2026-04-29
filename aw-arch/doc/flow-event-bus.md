# FlowEventBus 事件总线

基于 `SharedFlow` 的事件总线，支持普通事件和粘性事件，类型安全，自动清理。

## 核心特性

- **类型安全**：以事件类型（`KClass`）作为通道 key
- **普通事件**：`post` / `tryPost` 发送，`observe` 订阅，仅新订阅者收到后续事件
- **粘性事件**：`postSticky` / `tryPostSticky` 发送，新订阅者立即收到最近一条
- **自动清理**：某类型事件无订阅者超过 `autoCleanupDelay` 后，自动释放资源
- **线程安全**：所有操作均可在任意线程调用

## 发送事件

### 普通事件

```kotlin
// 挂起式发送（推荐）
FlowEventBus.post(LoginSuccessEvent("user123"))

// 非挂起式发送（缓冲区满时返回 false）
val success = FlowEventBus.tryPost(LoginSuccessEvent("user123"))
```

### 粘性事件

```kotlin
// 新订阅者会立即收到最近一条粘性事件
FlowEventBus.postSticky(ThemeChangedEvent(darkMode = true))
FlowEventBus.tryPostSticky(ThemeChangedEvent(darkMode = true))
```

## 订阅事件

### 普通事件

```kotlin
// 方式一：直接使用 Flow
FlowEventBus.observe<LoginSuccessEvent>()
    .collectOnLifecycle(this) { event ->
        updateUI(event.userId)
    }

// 方式二：使用 LifecycleOwner 扩展（推荐）
observeEvent<LoginSuccessEvent> { event ->
    updateUI(event.userId)
}
```

### 粘性事件

```kotlin
// 方式一
FlowEventBus.observeSticky<ThemeChangedEvent>()
    .collectOnLifecycle(this) { event ->
        applyTheme(event.darkMode)
    }

// 方式二：使用扩展
observeStickyEvent<ThemeChangedEvent> { event ->
    applyTheme(event.darkMode)
}
```

## 移除粘性事件

```kotlin
FlowEventBus.removeSticky<ThemeChangedEvent>()
```

## 清理

```kotlin
// 清除指定类型的所有事件（普通 + 粘性）
FlowEventBus.clear<LoginSuccessEvent>()

// 清除所有事件
FlowEventBus.clearAll()
```

## 自动清理机制

- 默认延迟 30000ms（30秒）
- 仅当某类型事件**曾经有过订阅者**且当前订阅数归零超过延迟时间后，才释放对应通道
- 普通/粘性通道分别独立清理
- 可通过 `AwArch.init` 配置：

```kotlin
AwArch.init {
    flowEventBusAutoCleanupDelayMs = 60_000L  // 60秒
}
```

- 设为 0 或负数可禁用自动清理

## 事件定义

```kotlin
// 普通事件
data class LoginSuccessEvent(val userId: String)

// 粘性事件（如主题切换、配置变更）
data class ThemeChangedEvent(val darkMode: Boolean)
```

事件类型可以是任意 `Any` 子类，建议使用 `data class`。

## 注意事项

- `post()` 内部使用 `scope.launch`，高频场景（如埋点风暴）建议使用 `tryPost()` 或在调用方合并
- `autoCleanup` 仅在订阅者归零后延迟清理，仅调用 `observe` 取得 Flow 但未 collect 时**不会**触发清理
- 粘性事件的 `replay = 1`，新订阅者只会收到最近一条
- `observeEvent` 扩展默认在 `STARTED` 状态收集，离开后自动暂停
