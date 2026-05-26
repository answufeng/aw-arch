# Changelog

## 1.1.0

在 1.0.0 基础上的功能与修复发布（JitPack tag `1.1.0`）。

### 库

- **MviEffect**：MVI 一次性副作用正式类型；`mvi.UiEvent` 保留为 deprecated typealias
- **AwNav**：修复 `singleTop` 在 Fragment 已清空时静默跳过导航；`clearStack` 同步移除容器 Fragment；子容器 `init(host)` 在 view 未创建时明确报错
- **MVP**：`MvpActivity` 与 Fragment 一致，Presenter 由 `ViewModel` 持有，配置变更后保留
- **AwNavTabSwitcher**、**BackDispatcherChain**、**AwNavHostFragment**、**ArchView**
- Hilt 基类合并为薄封装（继承非 Hilt 基类 + `injectViewModel` / `injectPresenter`）

### 文档与 Demo

- 文档对齐 `MviEffect`；README 补充 AwNav 依赖、Demo 结构、集成踩坑
- Demo 主界面五入口 Hub；Hilt + SimpleMVI 示例；AwNav 页统一 Toolbar 与中文字串

## 1.0.0

首个公开发布（JitPack）。

### 架构基类

- **MVVM**：`MvvmViewModel` + Activity / Fragment / Dialog / BottomSheet；`UiEvent` 一次性事件；`MvvmAwNavDispatch` 导航辅助
- **MVI**：`MviViewModel` + 全容器基类；一次性副作用类型 **`MviEffect`**（`mvi.UiEvent` 为 deprecated typealias）
- **SimpleMVI**：无独立 Effect 的 `SimpleMviViewModel` 及对应基类
- **MVP**：`MvpPresenter` + 基类；Fragment 与 **Activity** 均通过 `ViewModel` 持有 Presenter，配置变更后保留
- **Hilt**：`HiltMvvm*` / `HiltMvi*` / `HiltSimpleMvi*` / `HiltMvp*` 薄封装（`injectViewModel` / `injectPresenter`）

### AwNav

- Activity / Fragment 子容器 `AwNav.init`
- 路由注册、拦截器、返回栈、`NavOptions`（`singleTop`、`backStackName`、动画）
- **`singleTop`**：仅当容器内存在对应 tag 的 Fragment 时复用；路由名相同但 Fragment 已被清空时会正常重新导航
- **`clearStack`**：清空返回栈并移除容器内当前 Fragment，同步 `currentRoute`
- Fragment 子容器 `init(host)` 须在 **`onViewCreated` 之后**（view 已创建）
- **`AwNavTabSwitcher`**：多 Tab 独立返回栈（`saveBackStack` / `restoreBackStack`）
- **`BackDispatcherChain`**：嵌套返回分发

### 其他

- **FlowEventBus**、**LoadState**、Flow / 生命周期扩展
- **ArchView**：MVVM / MVP 共用 UI 契约（Toast、返回等）
- **ViewBindingDelegate**（`by viewBinding()`）仍保留，新代码可用各模式基类内置 Binding

### Demo

- 主界面五入口：架构模式 / Hilt / LoadState / FlowEventBus / AwNav
- Hilt 菜单含 **Hilt + SimpleMVI** 示例
- AwNav 基础路由与拦截器页统一 Toolbar 壳层与中文字串

### 依赖说明

- AwNav 依赖 **`androidx.fragment:fragment-ktx`**（`commit { }` 等）；宿主若未通过其他库间接引入，请显式添加（Demo 已声明）

详见 [README.md](README.md) 与 `aw-arch/doc/` 各模块文档。
