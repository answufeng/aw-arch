# Demo 应用混淆规则（Hilt / 反射由各库自带 consumer 规则处理，此处仅保留演示所需最小补充）

-keepattributes *Annotation*
-dontwarn org.conscrypt.**

# AwNav FragmentFactory.instantiate 依赖完整类名（演示包体量小，整包保留微信模块）
-keep class com.answufeng.arch.demo.wechat.** { *; }
