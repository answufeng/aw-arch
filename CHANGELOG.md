# Changelog

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
