# Hilt 集成

aw-arch 提供与 Hilt 依赖注入框架集成的基类，ViewModel/Presenter 通过 Hilt 自动注入，无需反射创建。

## 前置条件

1. 添加 Hilt 依赖和插件
2. Application 类添加 `@HiltAndroidApp`
3. Activity/Fragment 添加 `@AndroidEntryPoint`

```kotlin
@HiltAndroidApp
class MyApp : Application() { ... }
```

## MVVM + Hilt

### HiltMvvmActivity

```kotlin
@AndroidEntryPoint
class HiltDemoActivity : HiltMvvmActivity<ActivityHiltDemoBinding, HiltDemoViewModel>() {
    override val viewModel: HiltDemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityHiltDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnPing.setOnClickListener { viewModel.showHiltMessage() }
    }
}
```

### HiltMvvmFragment

```kotlin
@AndroidEntryPoint
class HiltDemoFragment : HiltMvvmFragment<FragmentHiltDemoBinding, HiltDemoViewModel>() {
    override val viewModel: HiltDemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHiltDemoBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) { ... }
}
```

### ViewModel

```kotlin
@HiltViewModel
class HiltDemoViewModel @Inject constructor() : MvvmViewModel() {
    fun showHiltMessage() {
        showToast("ViewModel 由 Hilt 注入")
    }
}
```

## MVI + Hilt

### HiltMviActivity

```kotlin
@AndroidEntryPoint
class HiltMviDemoActivity : HiltMviActivity<VB, State, Event, Intent, VM>() {
    override val viewModel: VM by viewModels()
    override fun inflateBinding(inflater: LayoutInflater) = ...
    override fun initView(savedInstanceState: Bundle?) { ... }
    override fun render(state: State) { ... }
}
```

### HiltMviFragment

```kotlin
@AndroidEntryPoint
class HiltMviDemoFragment : HiltMviFragment<VB, S, E, I, VM>() {
    override val viewModel: VM by viewModels()
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) = ...
    override fun initView(savedInstanceState: Bundle?) { ... }
    override fun render(state: S) { ... }
}
```

## MVP + Hilt

### HiltMvpActivity

```kotlin
@AndroidEntryPoint
class HiltMvpDemoActivity : HiltMvpActivity<VB, V, P>() {
    override val presenter: P by viewModels() // 或其他注入方式
    override fun inflateBinding(inflater: LayoutInflater) = ...
    override fun initView(savedInstanceState: Bundle?) { ... }
}
```

## 基类列表

| 基类 | 模式 | 容器 |
|------|------|------|
| `HiltMvvmActivity<VB, VM>` | MVVM | AppCompatActivity |
| `HiltMvvmFragment<VB, VM>` | MVVM | Fragment |
| `HiltMvvmDialogFragment<VB, VM>` | MVVM | DialogFragment |
| `HiltMvvmBottomSheetDialogFragment<VB, VM>` | MVVM | BottomSheetDialogFragment |
| `HiltMviActivity<VB, S, E, I, VM>` | MVI | AppCompatActivity |
| `HiltMviFragment<VB, S, E, I, VM>` | MVI | Fragment |
| `HiltMviDialogFragment<VB, S, E, I, VM>` | MVI | DialogFragment |
| `HiltMviBottomSheetDialogFragment<VB, S, E, I, VM>` | MVI | BottomSheetDialogFragment |
| `HiltMvpActivity<VB, V, P>` | MVP | AppCompatActivity |
| `HiltMvpFragment<VB, V, P>` | MVP | Fragment |
| `HiltMvpDialogFragment<VB, V, P>` | MVP | DialogFragment |
| `HiltMvpBottomSheetDialogFragment<VB, V, P>` | MVP | BottomSheetDialogFragment |

## 与非 Hilt 版本的区别

`HiltMvvm*` / `HiltMvi*` / `HiltSimpleMvi*` / `HiltMvp*` **直接继承**对应非 Hilt 基类，生命周期、`render`、`attachView` 等行为与非 Hilt 完全一致。

| 特性 | 非 Hilt 版本 | Hilt 版本 |
|------|-------------|-----------|
| ViewModel 创建 | 反射推断 + `ViewModelProvider` | `by viewModels()` 注入 |
| Presenter 创建 | 反射推断无参构造（Fragment 由 ViewModel 持有） | `override val presenter` + `injectPresenter()` |
| `viewModel` | `open val`，内部缓存 | `abstract override val viewModel`，经 `injectViewModel()` 注入 |
| `presenter` | `open val`，反射或 ViewModel 持有 | `abstract override val presenter`，经 `injectPresenter()` 注入 |
| 自定义注入 | 覆写 `injectViewModel()` / `injectPresenter()` 或 `obtainViewModel()` | Hilt 子类默认委托至 `viewModel` / `presenter` |
| `createViewModel()` | 可覆写 | 不需要 |

```kotlin
// 非 Hilt：手动提供 ViewModel（可选）
class MyActivity : MvvmActivity<..., MyVm>() {
    override fun injectViewModel(): MyVm? = myManualVm
}

// Hilt：与之前相同
@AndroidEntryPoint
class HiltDemoActivity : HiltMvvmActivity<..., HiltDemoViewModel>() {
    override val viewModel: HiltDemoViewModel by viewModels()
}
```

## 注意事项

- Hilt 基类中 `viewModel` 为 `abstract override val`，必须由子类通过 `by viewModels()` 或 `@Inject` 提供
- `@HiltViewModel` 的 ViewModel 必须有 `@Inject constructor()`
- Fragment 中使用 `by viewModels()` 获取的是 Fragment 作用域的 ViewModel，如需 Activity 作用域使用 `by activityViewModels()`
