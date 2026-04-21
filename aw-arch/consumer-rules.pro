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

# MviViewModel
-keep class com.answufeng.arch.mvi.MviViewModel { *; }
-keep class com.answufeng.arch.mvi.SimpleMviViewModel { *; }
-keep class com.answufeng.arch.mvi.NoEvent { *; }
-keep interface com.answufeng.arch.mvi.MviDispatcher { *; }

# MVVM
-keep class com.answufeng.arch.base.MvvmViewModel { *; }
-keep class com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keep class * extends com.answufeng.arch.base.MvvmViewModel$UiEvent { *; }
-keep interface com.answufeng.arch.mvvm.MvvmView { *; }

# BaseViewModel
-keep class com.answufeng.arch.base.BaseViewModel { *; }

# BaseActivity / BaseFragment
-keep class com.answufeng.arch.base.BaseActivity { *; }
-keep class com.answufeng.arch.base.BaseFragment { *; }

# LoadState sealed class
-keep class com.answufeng.arch.state.LoadState { *; }
-keep class * extends com.answufeng.arch.state.LoadState { *; }

# AwArch config
-keep class com.answufeng.arch.config.AwArch { *; }
-keep interface com.answufeng.arch.config.AwLogger { *; }

# AwNav
-keepclassmembers class com.answufeng.arch.nav.AwNav { *; }
-keep class com.answufeng.arch.nav.NavOptions { *; }
-keep class com.answufeng.arch.nav.NavAnim { *; }
-keep interface com.answufeng.arch.nav.NavInterceptor { *; }
-keep class com.answufeng.arch.nav.NavRouteBuilder { *; }

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
