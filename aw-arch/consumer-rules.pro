# aw-arch consumer ProGuard rules

# BaseViewModel 及其子类通过反射创建
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# FlowEventBus 使用 class name 作为 key
-keep class com.answufeng.arch.event.FlowEventBus { *; }

# MVI 标记接口
-keep interface com.answufeng.arch.mvi.UiState
-keep interface com.answufeng.arch.mvi.UiEvent
-keep interface com.answufeng.arch.mvi.UiIntent
-keep class * implements com.answufeng.arch.mvi.UiState { public *; }
-keep class * implements com.answufeng.arch.mvi.UiEvent { public *; }
-keep class * implements com.answufeng.arch.mvi.UiIntent { public *; }

# MviViewModel & SimpleMviViewModel
-keep class com.answufeng.arch.mvi.MviViewModel { *; }
-keep class com.answufeng.arch.mvi.MviViewModel$* { *; }
-keep class com.answufeng.arch.mvi.SimpleMviViewModel { *; }
-keep class com.answufeng.arch.mvi.SimpleMviViewModel$* { *; }
-keep class com.answufeng.arch.mvi.NoEvent { *; }
-keep interface com.answufeng.arch.mvi.MviDispatcher { *; }

# MVI Activity/Fragment 基类
-keep class com.answufeng.arch.mvi.MviActivity { *; }
-keep class com.answufeng.arch.mvi.MviFragment { *; }
-keep class com.answufeng.arch.mvi.MviDialogFragment { *; }
-keep class com.answufeng.arch.mvi.MviBottomSheetDialogFragment { *; }
-keep class com.answufeng.arch.mvi.SimpleMviActivity { *; }
-keep class com.answufeng.arch.mvi.SimpleMviFragment { *; }
-keep class com.answufeng.arch.mvi.SimpleMviDialogFragment { *; }
-keep class com.answufeng.arch.mvi.SimpleMviBottomSheetDialogFragment { *; }

# MVVM Activity/Fragment 基类
-keep class com.answufeng.arch.mvvm.MvvmActivity { *; }
-keep class com.answufeng.arch.mvvm.MvvmFragment { *; }
-keep class com.answufeng.arch.mvvm.MvvmDialogFragment { *; }
-keep class com.answufeng.arch.mvvm.MvvmBottomSheetDialogFragment { *; }
-keep class com.answufeng.arch.base.MvvmViewModel { *; }
-keep class com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keep class * extends com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keep interface com.answufeng.arch.mvvm.MvvmView { *; }

# Hilt 集成的基类
-keep class com.answufeng.arch.hilt.HiltMvvmActivity { *; }
-keep class com.answufeng.arch.hilt.HiltMvvmFragment { *; }
-keep class com.answufeng.arch.hilt.HiltMvvmDialogFragment { *; }
-keep class com.answufeng.arch.hilt.HiltMvvmBottomSheetDialogFragment { *; }
-keep class com.answufeng.arch.hilt.HiltMviActivity { *; }
-keep class com.answufeng.arch.hilt.HiltMviFragment { *; }
-keep class com.answufeng.arch.hilt.HiltMviDialogFragment { *; }
-keep class com.answufeng.arch.hilt.HiltMviBottomSheetDialogFragment { *; }

# BaseViewModel & BaseActivity & BaseFragment
-keep class com.answufeng.arch.base.BaseViewModel { *; }
-keep class com.answufeng.arch.base.BaseActivity { *; }
-keep class com.answufeng.arch.base.BaseFragment { *; }

# LoadState sealed class
-keep class com.answufeng.arch.state.LoadState { *; }
-keep class com.answufeng.arch.state.LoadState$* { *; }
-keep class * extends com.answufeng.arch.state.LoadState { *; }

# AwArch config
-keep class com.answufeng.arch.config.AwArch { *; }
-keep interface com.answufeng.arch.config.AwLogger { *; }
-keep class com.answufeng.arch.config.DefaultAwLogger { *; }
-keep interface com.answufeng.arch.config.AwArchDsl { *; }

# AwNav
-keep class com.answufeng.arch.nav.AwNav { *; }
-keep class com.answufeng.arch.nav.AwNav$* { *; }
-keep class com.answufeng.arch.nav.NavOptions { *; }
-keep class com.answufeng.arch.nav.NavAnim { *; }
-keep interface com.answufeng.arch.nav.NavInterceptor { *; }
-keep class com.answufeng.arch.nav.NavRouteBuilder { *; }

# ViewBinding 委托
-keep class com.answufeng.arch.ext.ViewBindingDelegateKt { *; }

# Extension functions
-keepclassmembers class com.answufeng.arch.ext.FlowExtKt { *; }
-keepclassmembers class com.answufeng.arch.ext.LifecycleExtKt { *; }
-keepclassmembers class com.answufeng.arch.ext.ViewModelExtKt { *; }
-keepclassmembers class com.answufeng.arch.state.LoadStateKt { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin 元数据
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
