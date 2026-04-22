# aw-arch consumer ProGuard rules
# 此文件中的规则会被自动合并到宿主应用的混淆配置中
# 仅保留反射、泛型推断、序列化等必要入口，避免过度 -keep

# ===========================================================
# 保留泛型签名（inferViewModelClass 通过反射读取泛型参数）
# ===========================================================
-keepattributes Signature

# ===========================================================
# ViewModel 构造函数保留
# inferViewModelClass 需要读取泛型参数中的 ViewModel 类型，
# ViewModelProvider 需要 public 构造函数
# ===========================================================
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ===========================================================
# MVI 标记接口及实现类（仅保留构造器，避免整类 public * 过宽）
# ===========================================================
-keepclassmembers class * implements com.answufeng.arch.mvi.UiState {
    <init>(...);
}
-keepclassmembers class * implements com.answufeng.arch.mvi.UiEvent {
    <init>(...);
}
-keepclassmembers class * implements com.answufeng.arch.mvi.UiIntent {
    <init>(...);
}

# ===========================================================
# MVI ViewModel 核心成员
# 保留构造函数、handleIntent、状态更新方法
# ===========================================================
-keepclassmembers class com.answufeng.arch.mvi.MviViewModel {
    <init>(...);
    void handleIntent(...);
    kotlinx.coroutines.flow.StateFlow getState();
    void updateState(...);
    void sendMviEvent(...);
}
-keepclassmembers class com.answufeng.arch.mvi.SimpleMviViewModel {
    <init>(...);
}
-keepclassmembers class com.answufeng.arch.mvi.NoEvent { *; }
-keepclassmembers interface com.answufeng.arch.mvi.MviDispatcher { *; }

# ===========================================================
# MVI Activity/Fragment 基类
# 保留构造函数和核心回调方法
# ===========================================================
-keepclassmembers class com.answufeng.arch.mvi.MviActivity {
    <init>(...);
    void render(...);
    void handleEvent(...);
}
-keepclassmembers class com.answufeng.arch.mvi.MviFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
}
-keepclassmembers class com.answufeng.arch.mvi.MviDialogFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
}
-keepclassmembers class com.answufeng.arch.mvi.MviBottomSheetDialogFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
}
-keepclassmembers class com.answufeng.arch.mvi.SimpleMviActivity {
    <init>(...);
    void render(...);
}
-keepclassmembers class com.answufeng.arch.mvi.SimpleMviFragment {
    <init>(...);
    void render(...);
}
-keepclassmembers class com.answufeng.arch.mvi.SimpleMviDialogFragment {
    <init>(...);
    void render(...);
}
-keepclassmembers class com.answufeng.arch.mvi.SimpleMviBottomSheetDialogFragment {
    <init>(...);
    void render(...);
}

# ===========================================================
# MVVM ViewModel & View 接口
# ===========================================================
-keepclassmembers class com.answufeng.arch.base.MvvmViewModel {
    <init>(...);
    kotlinx.coroutines.flow.Flow getUiEvent();
    void sendEvent(...);
    void showToast(...);
    void showLoading(...);
    void navigate(...);
    void navigateBack();
}
-keepclassmembers class com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keepclassmembers class * extends com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keepclassmembers interface com.answufeng.arch.mvvm.MvvmView { *; }

# ===========================================================
# MVVM Activity/Fragment 基类
# ===========================================================
-keepclassmembers class com.answufeng.arch.mvvm.MvvmActivity {
    <init>(...);
    void initView(...);
    void initObservers();
}
-keepclassmembers class com.answufeng.arch.mvvm.MvvmFragment {
    <init>(...);
    void initView(...);
    void initObservers();
    void onLazyLoad();
}
-keepclassmembers class com.answufeng.arch.mvvm.MvvmDialogFragment {
    <init>(...);
    void initView(...);
    void initObservers();
}
-keepclassmembers class com.answufeng.arch.mvvm.MvvmBottomSheetDialogFragment {
    <init>(...);
    void initView(...);
    void initObservers();
}

# ===========================================================
# Hilt 集成基类
# Hilt 编译器会自动处理 @Inject 和 @AndroidEntryPoint，
# 此处仅保留基类的核心回调方法
# ===========================================================
-keepclassmembers class com.answufeng.arch.hilt.HiltMvvmActivity {
    <init>(...);
    void initView(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMvvmFragment {
    <init>(...);
    void initView(...);
    void onLazyLoad();
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMvvmDialogFragment {
    <init>(...);
    void initView(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMvvmBottomSheetDialogFragment {
    <init>(...);
    void initView(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMviActivity {
    <init>(...);
    void render(...);
    void handleEvent(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMviFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
    void onLazyLoad();
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMviDialogFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}
-keepclassmembers class com.answufeng.arch.hilt.HiltMviBottomSheetDialogFragment {
    <init>(...);
    void render(...);
    void handleEvent(...);
    androidx.lifecycle.ViewModelProvider$Factory getViewModel();
}

# ===========================================================
# 基础类 (BaseActivity/BaseFragment/BaseViewModel)
# ===========================================================
-keepclassmembers class com.answufeng.arch.base.BaseViewModel {
    <init>(...);
    void handleException(...);
}
-keepclassmembers class com.answufeng.arch.base.BaseActivity {
    <init>(...);
    void initView(...);
    void initObservers();
}
-keepclassmembers class com.answufeng.arch.base.BaseFragment {
    <init>(...);
    void initView(...);
    void initObservers();
    void onLazyLoad();
}

# ===========================================================
# LoadState sealed class
# 保留子类构造函数以便反序列化
# ===========================================================
-keepclassmembers class com.answufeng.arch.state.LoadState { *; }
-keepclassmembers class com.answufeng.arch.state.LoadState$Loading { *; }
-keepclassmembers class com.answufeng.arch.state.LoadState$Success { *; }
-keepclassmembers class com.answufeng.arch.state.LoadState$Error { *; }

# ===========================================================
# AwArch config
# ===========================================================
-keepclassmembers class com.answufeng.arch.config.AwArch { *; }
-keepclassmembers interface com.answufeng.arch.config.AwLogger { *; }
-keepclassmembers interface com.answufeng.arch.config.AwArchDsl { *; }

# ===========================================================
# AwNav
# ===========================================================
-keepclassmembers class com.answufeng.arch.nav.AwNav { *; }
-keepclassmembers class com.answufeng.arch.nav.AwNav$Companion { *; }
-keepclassmembers class com.answufeng.arch.nav.NavOptions { *; }
-keepclassmembers enum com.answufeng.arch.nav.NavAnim { *; }
-keepclassmembers interface com.answufeng.arch.nav.NavInterceptor { *; }
-keepclassmembers class com.answufeng.arch.nav.NavRouteBuilder { *; }

# ===========================================================
# FlowEventBus
# ===========================================================
-keepclassmembers class com.answufeng.arch.event.FlowEventBus { *; }
-keepclassmembers class com.answufeng.arch.event.FlowEventBus$Companion { *; }

# ===========================================================
# ViewBinding 委托 & 扩展函数
# ===========================================================
-keepclassmembers class com.answufeng.arch.ext.ViewBindingDelegateKt { *; }
-keepclassmembers class com.answufeng.arch.ext.FragmentViewBindingDelegate { *; }
-keepclassmembers class com.answufeng.arch.ext.ActivityViewBindingDelegate { *; }
-keepclassmembers class com.answufeng.arch.ext.FlowExtKt { *; }
-keepclassmembers class com.answufeng.arch.ext.LifecycleExtKt { *; }
-keepclassmembers class com.answufeng.arch.ext.ViewModelExtKt { *; }
-keepclassmembers class com.answufeng.arch.state.LoadStateKt { *; }

# ===========================================================
# Kotlin coroutines
# ===========================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
