# 基类体系

aw-arch 提供完整的 Activity/Fragment 基类体系，自动管理 ViewBinding 生命周期、ViewModel 创建、懒加载等。

## BaseActivity

最基础的 Activity 基类，仅提供 ViewBinding 支持：

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected val binding: VB
    abstract fun inflateBinding(): VB
    abstract fun initView(savedInstanceState: Bundle?)
    open fun initObservers() {}
}
```

### 生命周期回调顺序

1. `inflateBinding()` → 2. `initView()` → 3. `initObservers()`

## BaseFragment

基础 Fragment 基类，自动管理 ViewBinding 生命周期并支持懒加载：

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected val binding: VB
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun initView(savedInstanceState: Bundle?)
    open fun initObservers() {}
    open fun onLazyLoad() {}
}
```

### 生命周期回调顺序

1. `inflateBinding()` → 2. `initView()` → 3. `initObservers()` → 4. `onLazyLoad()`（首次可见时）

## 懒加载机制

`LazyLoadHelper` 实现每次新建 View 后的首次 `onResume` 触发一次懒加载：

- 从返回栈恢复、配置变更等导致 View 重建时，会重新触发 `onLazyLoad`
- 进程恢复后仍能正确再加载一次
- 不需要懒加载时，不覆写 `onLazyLoad()` 即可

## ViewBinding 生命周期

### Activity

- `onCreate` 中创建 binding 并 `setContentView`
- `onDestroy` 中置空 `_binding`

### Fragment

- `onCreateView` 中创建 binding
- `onDestroyView` 中置空 `_binding`
- 在 `onDestroyView` 之后访问 `binding` 会抛出 `IllegalStateException`

## 架构基类对照表

| 架构 | Activity | Fragment | DialogFragment | BottomSheet |
|------|----------|----------|----------------|-------------|
| Base | `BaseActivity<VB>` | `BaseFragment<VB>` | - | - |
| MVVM | `MvvmActivity<VB, VM>` | `MvvmFragment<VB, VM>` | `MvvmDialogFragment<VB, VM>` | `MvvmBottomSheetDialogFragment<VB, VM>` |
| MVI | `MviActivity<VB, S, E, I, VM>` | `MviFragment<VB, S, E, I, VM>` | `MviDialogFragment<VB, S, E, I, VM>` | `MviBottomSheetDialogFragment<VB, S, E, I, VM>` |
| SimpleMVI | `SimpleMviActivity<VB, S, I, VM>` | `SimpleMviFragment<VB, S, I, VM>` | `SimpleMviDialogFragment<VB, S, I, VM>` | `SimpleMviBottomSheetDialogFragment<VB, S, I, VM>` |
| MVP | `MvpActivity<VB, V, P>` | `MvpFragment<VB, V, P>` | `MvpDialogFragment<VB, V, P>` | `MvpBottomSheetDialogFragment<VB, V, P>` |

## Hilt 版本对照表

| 架构 | Activity | Fragment | DialogFragment | BottomSheet |
|------|----------|----------|----------------|-------------|
| MVVM | `HiltMvvmActivity<VB, VM>` | `HiltMvvmFragment<VB, VM>` | `HiltMvvmDialogFragment<VB, VM>` | `HiltMvvmBottomSheetDialogFragment<VB, VM>` |
| MVI | `HiltMviActivity<VB, S, E, I, VM>` | `HiltMviFragment<VB, S, E, I, VM>` | `HiltMviDialogFragment<VB, S, E, I, VM>` | `HiltMviBottomSheetDialogFragment<VB, S, E, I, VM>` |
| MVP | `HiltMvpActivity<VB, V, P>` | `HiltMvpFragment<VB, V, P>` | `HiltMvpDialogFragment<VB, V, P>` | `HiltMvpBottomSheetDialogFragment<VB, V, P>` |

## 选型建议

| 场景 | 推荐 |
|------|------|
| 简单页面，不需要 ViewModel | `BaseActivity` / `BaseFragment` |
| 传统 MVVM，需要一次性事件 | `MvvmActivity` / `MvvmFragment` |
| 严格单向数据流，复杂状态管理 | `MviActivity` / `MviFragment` |
| 简单 MVI，不需要 Event | `SimpleMviActivity` / `SimpleMviFragment` |
| 传统 MVP，已有 Contract 约定 | `MvpActivity` / `MvpFragment` |
| 使用 Hilt 依赖注入 | `Hilt*` 系列 |
