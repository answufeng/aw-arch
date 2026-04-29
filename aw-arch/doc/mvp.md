# MVP 模式

aw-arch 提供传统 MVP 架构基类，基于 `BaseMvpPresenter` + `MvpActivity/Fragment/Dialog` 等基类，Presenter 自动创建与生命周期绑定。

## 核心组件

### MvpView 接口

```kotlin
interface MvpView {
    fun onLoading(show: Boolean) {}
    fun showToast(message: String) {}
    fun navigateTo(route: String, extras: Bundle? = null) {}
    fun navigateBack() {}
}
```

### MvpPresenter 接口

```kotlin
interface MvpPresenter<V : MvpView> {
    fun attachView(view: V)
    fun detachView()
    val isViewAttached: Boolean
}
```

### BaseMvpPresenter

内置协程作用域，`detachView` 时自动 cancel：

```kotlin
class CounterPresenter : BaseMvpPresenter<CounterContract.View>() {
    private var count = 0

    fun increment() {
        count++
        viewOrNull?.render(count)
    }

    fun loadData() = launchIO {
        val data = repository.fetch()
        withMain { viewOrNull?.render(data) }
    }
}
```

#### 协程方法

| 方法 | 调度器 | 说明 |
|------|--------|------|
| `launch { }` | Main | UI 相关操作 |
| `launchIO { }` | IO | 网络请求、数据库操作 |
| `launchDefault { }` | Default | CPU 密集型计算 |
| `withMain { }` | Main | 切回主线程 |

#### 生命周期

| 时机 | 方法 |
|------|------|
| View 创建 | `attachView(view)` → `onViewAttached(view)` |
| View 销毁 | `detachView()` → `onViewDetached(view)` + `presenterScope.cancel()` |

## 基类列表

| 基类 | 容器 | 特性 |
|------|------|------|
| `MvpActivity<VB, V, P>` | AppCompatActivity | ViewBinding + Presenter 自动创建 |
| `MvpFragment<VB, V, P>` | Fragment | + 懒加载 |
| `MvpDialogFragment<VB, V, P>` | DialogFragment | 对话框场景 |
| `MvpBottomSheetDialogFragment<VB, V, P>` | BottomSheetDialogFragment | 底部弹窗场景 |

## 快速上手

```kotlin
// 1. 定义 Contract
interface CounterContract {
    interface View : MvpView {
        fun render(count: Int)
    }
}

// 2. 实现 Presenter
class CounterPresenter : BaseMvpPresenter<CounterContract.View>() {
    private var count = 0

    fun increment() {
        count++
        viewOrNull?.render(count)
    }
}

// 3. 实现 Activity
class CounterActivity :
    MvpActivity<ActivityCounterBinding, CounterContract.View, CounterPresenter>(),
    CounterContract.View {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityCounterBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnInc.setOnClickListener { presenter.increment() }
    }

    override fun render(count: Int) {
        binding.tvCount.text = count.toString()
    }
}
```

## Presenter 创建方式

### 反射自动创建（默认）

Presenter 必须有无参构造函数：

```kotlin
class MyPresenter : BaseMvpPresenter<MyContract.View>() {
    // 无参构造
}
```

### 手动创建

如果 Presenter 需要构造参数，覆写 `createPresenter()`：

```kotlin
class MyActivity : MvpActivity<VB, V, MyPresenter>() {
    override fun createPresenter(): MyPresenter {
        return MyPresenter(repository)
    }
}
```

## Hilt 版本

使用 `HiltMvpActivity` / `HiltMvpFragment`，Presenter 通过 Hilt 注入：

```kotlin
@AndroidEntryPoint
class HiltMvpDemoActivity : HiltMvpActivity<VB, V, MyPresenter>() {
    override val presenter: MyPresenter by viewModels() // 或通过 @Inject 注入
    override fun inflateBinding(inflater: LayoutInflater) = ...
    override fun initView(savedInstanceState: Bundle?) { ... }
}
```

## 注意事项

- Presenter 通过反射自动创建时，必须有无参构造函数
- `viewOrNull` 可能为 null（View 已 detach），访问前需判空
- `presenterScope` 在 `detachView` 时自动 cancel，`attachView` 时重建
- Fragment 中 `detachView` 在 `onDestroyView` 调用，Activity 中在 `onDestroy` 调用
