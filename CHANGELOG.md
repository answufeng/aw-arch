# Changelog

## 2.0.0

### 破坏性变更
- `BaseViewModel` 不再包含 UI 事件方法（`sendEvent`/`showToast`/`showLoading`/`navigate`/`navigateBack`），仅提供协程能力
- 新增 `MvvmViewModel` 作为 MVVM 模式的 ViewModel 基类，继承 `BaseViewModel` 并添加 UI 事件能力
- `BaseViewModel.UIEvent` 移至 `MvvmViewModel.UIEvent`
- `MviView.mviViewModel` 重命名为 `viewModel`
- `MvvmView.mvvmViewModel` 重命名为 `viewModel`
- MVVM 模式的 ViewModel 需从 `BaseViewModel` 迁移到 `MvvmViewModel`

### 新增
- `MvvmViewModel`：MVVM 专用 ViewModel 基类，提供 `sendEvent`/`showToast`/`showLoading`/`navigate`/`navigateBack`
- Hilt 专用基类：`HiltMvvmActivity`/`HiltMviActivity`/`HiltMvvmFragment`/`HiltMviFragment` 等，使用 `defaultViewModelProviderFactory`
- `SimpleMviDialogFragment`/`SimpleMviBottomSheetDialogFragment`：SimpleMvi 补全变体
- `LoadState.recover(defaultValue)`：错误时恢复为默认值
- `LoadState.recoverWith(fn)`：错误时通过函数计算恢复值
- `LoadState.combine(other, transform)`：合并两个 LoadState
- `Flow.asLoadState()`：Flow\<T\> → Flow\<LoadState\<T\>\>，自动处理 Loading/Error
- `Flow.mapLoadState(transform)`：Flow 链式 LoadState 转换
- `LifecycleOwner.observeEvent<T>()`：便捷事件观察扩展，自动检查 Lifecycle 状态
- `LifecycleOwner.observeStickyEvent<T>()`：便捷粘性事件观察扩展
- `BrickTimeSource`：统一时间源，使用 `SystemClock.elapsedRealtime()`
- `StrictBrickTestRule`：使用 `StandardTestDispatcher` 的测试规则，用于验证 Loading 状态转换
- `@Immutable` 注解：`UiState`/`UiEvent`/`LoadState`/`MvvmViewModel.UIEvent` 添加 Compose 兼容注解
- `compose-runtime` 作为 `compileOnly` 依赖

### 修复
- 修复 `BrickNav` 静态 Map 持有 Activity 强引用的内存泄漏风险（改用 `WeakReference`）
- 修复 `BrickNav.executePendingTransactions()` 在嵌套导航场景可能导致 `IllegalStateException` 的问题（改用 `FragmentLifecycleCallbacks` 监听）
- 修复 `FragmentViewBindingDelegate` 在 view 销毁后仍可访问 binding 的 bug（改用 `viewLifecycleOwner` + 防御性检查）
- 修复 `MviViewModel` 双事件系统冲突（移除无用的 `_uiEvent` Channel 和 `final override` 拦截方法）
- 修复 `BaseViewModel.sendEvent` 每次调用创建新协程的性能问题（改用 `Channel.UNLIMITED` + `trySend`）
- 修复 `throttleFirst`/`dispatchThrottled` 使用 `System.nanoTime()/1_000_000` 的精度问题（改用 `SystemClock.elapsedRealtime()`）

### 改进
- ViewModel 层级重构：`BaseViewModel`（协程能力）→ `MvvmViewModel`（+UIEvent）和 `MviViewModel`（+State/Event/Intent）
- `MviView`/`MvvmView` 接口属性名统一为 `viewModel`，消除双属性困惑
- `MviViewModel.sendMviEvent` 改用 `Channel.UNLIMITED` + `trySend`，消除协程创建开销
- `LifecycleExt` 新增 `observeEvent`/`observeStickyEvent`，自动检查 `Lifecycle.State.DESTROYED`
- `BrickTestRule` 改为 `open class`，支持 `StrictBrickTestRule` 继承
- `BrickTestRule` 改用 `TestRule` 接口实现，替代 `TestWatcher`
- `consumer-rules.pro` 更新 ProGuard 规则引用 `MvvmViewModel$UIEvent`

## 1.1.0

### 新增
- Hilt 集成支持：`HiltMvvmActivity`、`HiltMviActivity`、`HiltMvvmFragment`、`HiltMviFragment`、`HiltMvvmDialogFragment`、`HiltMviDialogFragment`
- `FlowEventBus.tryPost()` / `tryPostSticky()` 非挂起发送方法
- `FlowEventBus.observe<T>()` / `observeSticky<T>()` 返回类型安全的 `SharedFlow<T>`，无需手动 cast
- `FlowEventBus.observeRaw()` / `observeStickyRaw()` 原始 API 保留
- `MviViewModel.clearThrottleCache()` 清除节流缓存方法
- `MviViewModel.onCleared()` 中自动清理 `intentThrottleMap`
- `MviView` 接口：MVI 视图层辅助接口，消除 Activity/Fragment/Dialog 重复代码
- `MvvmView` 接口：MVVM 视图层辅助接口，统一事件分发逻辑
- `retryLoadState()` 新增 `maxDelayMillis` 参数，防止指数退避延迟无限增长
- `loadStateCatching()` / `retryLoadState()` 失败时通过 `BrickArch.logger` 记录日志
- Demo 应用新增 MVVM Demo、MVI Counter Demo、BrickNav 导航 Demo

### 修复
- 修复 `consumer-rules.pro` 中包名错误（`com.ail.brick.arch` → `com.answufeng.arch`）
- 修复 `MviViewModel.intentThrottleMap` 内存泄漏（`onCleared` 时清理）
- 修复 `BrickNav.instances` 线程安全问题（`mutableMapOf` → `ConcurrentHashMap`）
- 修复 `BaseViewModel` 异常处理器重复创建（提取为 `exceptionHandler` 字段）
- 修复 `MviViewModel.handleException` 未调用 super 的问题

### 改进
- 重构 MVI/MVVM 基类，通过 `MviView`/`MvvmView` 接口消除重复的 collect 代码
- `BrickNav.navigate()` 错误信息包含可用路由列表
- `BrickNav.from()` 错误信息包含 Activity 类名
- `BrickNav` 防连点时间常量提取为 `NAV_THROTTLE_MILLIS`
- `FlowEventBus.observe` 扩展方法不再需要 `filterIsInstance`（已在内部处理）
- 完善 `consumer-rules.pro` ProGuard 规则
- README 全面重写，包含架构图、API 速览、完整示例

## 1.0.0

- Initial release
- Package: `com.answufeng.arch`
