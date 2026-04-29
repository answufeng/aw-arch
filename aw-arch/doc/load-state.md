# LoadState 状态管理

通用加载状态密封类，适用于任何异步数据加载场景。提供 `Loading` / `Success` / `Error` 三态，以及丰富的操作符和协程工具函数。

## 三种状态

```kotlin
sealed class LoadState<out T> {
    data object Loading : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error(val exception: Throwable, val message: String = ...) : LoadState<Nothing>()
}
```

## 基本用法

### 在 MVI State 中使用

```kotlin
data class HomeState(
    val items: LoadState<List<String>> = LoadState.Loading
) : UiState

// ViewModel 中
updateState { copy(items = LoadState.Loading) }
try {
    val data = repository.fetchItems()
    updateState { copy(items = LoadState.Success(data)) }
} catch (e: Exception) {
    updateState { copy(items = LoadState.Error(e)) }
}

// UI 中渲染
when (val items = state.items) {
    is LoadState.Loading -> showProgressBar()
    is LoadState.Success -> adapter.submitList(items.data)
    is LoadState.Error   -> showError(items.message)
}
```

### 属性判断

```kotlin
val isLoading: Boolean = state.items.isLoading
val isSuccess: Boolean = state.items.isSuccess
val isError: Boolean   = state.items.isError
```

## 操作符

### map — 转换成功数据

```kotlin
val names: LoadState<List<String>> = itemsState.map { it.map { item -> item.name } }
```

### getOrNull / getOrDefault — 安全取值

```kotlin
val data = state.items.getOrNull()
val data = state.items.getOrDefault(emptyList())
```

### fold — 三态模式匹配

```kotlin
state.items.fold(
    onLoading = { showProgressBar() },
    onSuccess = { render(it) },
    onError   = { showError(it) }
)
```

### onSuccess / onError / onLoading — 链式回调

```kotlin
state.items
    .onLoading { showProgressBar() }
    .onSuccess { render(it) }
    .onError { showError(it) }
```

### onEach — 统一处理

```kotlin
state.items.onEach { log("Current state: $it") }
```

### recover / recoverWith — 错误恢复

```kotlin
// 恢复为默认值
val safe = state.items.recover(emptyList())

// 根据异常计算恢复值
val safe = state.items.recoverWith { exception ->
    cachedData[exception]
}
```

### combine — 合并两个 LoadState

```kotlin
val combined = state.items.combine(state.userInfo)
// Loading + Any = Loading
// Error + Any = Error
// Success + Success = Success(Pair(data1, data2))
```

## 协程工具函数

### loadStateCatching

在协程中安全执行并包装为 `LoadState`：

```kotlin
val state = loadStateCatching { repository.fetchItems() }
```

### loadStateWithTimeout

带超时的异步加载：

```kotlin
val state = loadStateWithTimeout(5_000, timeoutMessage = "请求超时") {
    repository.fetchItems()
}
```

### retryLoadState

带指数退避重试的异步加载：

```kotlin
val state = retryLoadState(
    times = 3,              // 最大重试次数（不含首次）
    initialDelayMillis = 1000,  // 首次重试延迟
    factor = 2.0,           // 延迟递增因子
    maxDelayMillis = 30_000L    // 最大延迟上限
) {
    repository.fetchItems()
}
```

## Flow 集成

### asLoadState

将任意 `Flow<T>` 转换为 `Flow<LoadState<T>>`：

```kotlin
repository.getItemsFlow()
    .asLoadState()
    .collectOnLifecycle(this) { state ->
        when (state) {
            is LoadState.Loading -> showProgressBar()
            is LoadState.Success -> render(state.data)
            is LoadState.Error -> showError(state.message)
        }
    }
```

### mapLoadState

在 Flow 中转换 LoadState 数据：

```kotlin
itemsLoadStateFlow
    .mapLoadState { items -> items.map { it.name } }
```

## 注意事项

- `LoadState.Error` 的默认 `message` 取自 `exception.message`，为 null 时回退为 "未知错误"
- `retryLoadState` 不支持取消，如需取消请使用 Flow 版本
- `combine` 两个 Error 时，优先保留第一个 Error
- `asLoadState()` 会在开始时发射 `Loading`，然后发射 `Success` 或 `Error`
