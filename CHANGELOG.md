# Changelog

## 2.0.0

### 破坏性变更
- `BaseViewModel` 不再包含 UI 事件方法（`sendEvent`/`showToast`/`showLoading`/`navigate`/`navigateBack`），仅提供协程能力
- 新增 `MvvmViewModel` 作为 MVVM 模式的 ViewModel 基类，继承 `BaseViewModel` 并添加 UI 事件能力
- `BaseViewModel.UiEvent` 移至 `MvvmViewModel.UiEvent`
- `MviViewModel` 不再继承 UI 事件系统，移除 `final override` 拦截方法
- `MviViewModel.sendMviEvent` 改用 `Channel.UNLIMITED` + `trySend`，消除协程创建开销
- `MviViewModel.dispatchThrottled` 改用 `SystemClock.elapsedRealtime()` 替代 `System.nanoTime()/1_000_000`
- `MviActivity`/`MvvmActivity` 移除 `MainScope()`，改用 `lifecycleScope` + `repeatOnLifecycle(STARTED)`
- `MviActivity.handleEvent()` 改为 `open` 函数提供默认空实现
- `FlowEventBus.tryPost()` 移除 `runBlocking`，改用 `tryEmit`
- `FragmentViewBindingDelegate` 改用 inflate 函数引用方式，零反射
- `AwNav.instances` 改用 `WeakReference` 存储

### 新增
- `MvvmViewModel`：MVVM 专用 ViewModel 基类，提供 `sendEvent`/`showToast`/`showLoading`/`navigate`/`navigateBack`
- `SimpleMviViewModel`/`SimpleMviActivity`：简化版 MVI，不需要 Event 时减少泛型参数（5→3）
- `MvvmFragment`/`MviFragment`：Fragment 基类，支持 ViewBinding + ViewModel + 生命周期安全
- `MvvmDialogFragment`/`MviDialogFragment`：DialogFragment 基类
- `MvvmBottomSheetDialogFragment`/`MviBottomSheetDialogFragment`：BottomSheet 基类
- Hilt 专用基类：`HiltMvvmActivity`/`HiltMviActivity`/`HiltMvvmFragment`/`HiltMviFragment`
- Flow 扩展：`throttleFirst`/`debounceAction`/`select`/`throttleClicks`
- LifecycleOwner 扩展：`observeEvent`/`observeStickyEvent`/`launchOnStarted`/`launchOnResumed`
- `LoadState.recover(defaultValue)`：错误时恢复为默认值
- `LoadState.recoverWith(fn)`：错误时通过函数计算恢复值
- `LoadState.combine(other, transform)`：合并两个 LoadState
- `Flow.asLoadState()`：Flow\<T\> → Flow\<LoadState\<T\>\>，自动处理 Loading/Error
- `Flow.mapLoadState(transform)`：Flow 链式 LoadState 转换
- `FlowEventBus.tryPostSticky()`：非挂起粘性事件发送
- `FlowEventBus.removeSticky()`：移除粘性事件

### 修复
- 修复 `MviActivity`/`MvvmActivity` 使用 `MainScope()` 导致的生命周期不匹配问题
- 修复 `FlowEventBus.flows`/`stickyFlows` 非线程安全问题（改用 `ConcurrentHashMap`）
- 修复 `FlowEventBus.tryPost()` 使用 `runBlocking` 可能导致 ANR 的问题
- 修复 `FragmentViewBindingDelegate` 在 view 销毁后仍可访问 binding 的 bug
- 修复 `BaseViewModel.sendEvent` 每次调用创建新协程的性能问题（改用 `Channel.UNLIMITED` + `trySend`）
- 修复 `MviViewModel` 双事件系统冲突（移除继承自 BaseViewModel 的 `_UiEvent` Channel）
- 修复 `AwNav.instances` 持有 Activity 强引用的内存泄漏风险（改用 `WeakReference`）

### 改进
- ViewModel 层级重构：`BaseViewModel`（协程能力）→ `MvvmViewModel`（+UiEvent）和 `MviViewModel`（+State/Event/Intent）
- `MviActivity`/`MvvmActivity` 改用 `lifecycleScope` + `repeatOnLifecycle(STARTED)` 收集 Flow
- `MviActivity`/`MvvmActivity` 添加 `initObservers()` 回调
- `MviActivity` 添加 `dispatchThrottled()` 便捷方法
- `FlowEventBus.post` 使用 `SupervisorJob` 缓存 scope，避免重复创建
- `AwNav` 使用 `FragmentLifecycleCallbacks` 监听 Fragment 状态更新 `currentRoute`
- `FragmentViewBindingDelegate` 改用 inflate 函数引用，零反射
- Demo 微信 Fragment 改为使用 `BaseFragment` + ViewBinding

## 1.1.0

### 新增
- Hilt 集成支持：`HiltMvvmActivity`、`HiltMviActivity`、`HiltMvvmFragment`、`HiltMviFragment`、`HiltMvvmDialogFragment`、`HiltMviDialogFragment`
- `FlowEventBus.tryPost()` / `tryPostSticky()` 非挂起发送方法
- `FlowEventBus.observe<T>()` / `observeSticky<T>()` 返回类型安全的 `SharedFlow<T>`，无需手动 cast
- `MviViewModel.clearThrottleCache()` 清除节流缓存方法
- `MviViewModel.onCleared()` 中自动清理 `intentThrottleMap`
- `retryLoadState()` 新增 `maxDelayMillis` 参数，防止指数退避延迟无限增长
- `loadStateCatching()` / `retryLoadState()` 失败时通过 `AwArch.logger` 记录日志

### 修复
- 修复 `MviViewModel.intentThrottleMap` 内存泄漏（`onCleared` 时清理）
- 修复 `AwNav.instances` 线程安全问题（`mutableMapOf` → `ConcurrentHashMap`）
- 修复 `BaseViewModel` 异常处理器重复创建（提取为 `exceptionHandler` 字段）

## 1.0.0

- Initial release
- Package: `com.answufeng.arch`
