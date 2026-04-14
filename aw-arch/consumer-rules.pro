# aw-arch consumer ProGuard rules

# BaseViewModel 及其子类通过反射创建
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# FlowEventBus 使用 class name 作为 key
-keep class com.answufeng.arch.event.FlowEventBus { *; }

# MVI 标记接口
-keep interface com.answufeng.arch.mvi.UiState
-keep interface com.answufeng.arch.mvi.UiEvent
-keep interface com.answufeng.arch.mvi.UiIntent
-keep class * implements com.answufeng.arch.mvi.UiState { *; }
-keep class * implements com.answufeng.arch.mvi.UiEvent { *; }
-keep class * implements com.answufeng.arch.mvi.UiIntent { *; }

# AwNav 路由注册使用 KClass
-keepclassmembers class com.answufeng.arch.nav.AwNav { *; }
-keep class com.answufeng.arch.nav.NavOptions { *; }
-keep class com.answufeng.arch.nav.NavAnim { *; }

# BaseViewModel.UIEvent 密封类子类
-keep class * extends com.answufeng.arch.base.BaseViewModel$UIEvent { *; }

# Kotlin 元数据
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
