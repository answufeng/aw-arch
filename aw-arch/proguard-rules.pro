# aw-arch ProGuard Rules
# 此文件用于库自身的 release 构建混淆规则
# Consumer-facing rules（供使用者混淆时使用）位于 consumer-rules.pro

# ===========================================================
# 保留 Kotlin 元数据和注解
# ===========================================================
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keep class kotlin.Metadata { *; }

# ===========================================================
# 保留枚举和 Serializable
# ===========================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===========================================================
# AwNav 内部数据结构
# routes 使用 KClass<out Fragment> 作为 value，
# FragmentFactory.instantiate 需要完整类名
# ===========================================================
-keepclassmembers class com.answufeng.arch.nav.AwNav {
    java.util.Map routes;
    java.util.List interceptors;
}

# ===========================================================
# FlowEventBus 内部 ConcurrentHashMap
# key 使用 KClass<*>，需确保反射可用
# ===========================================================
-keepclassmembers class com.answufeng.arch.event.FlowEventBus {
    java.util.concurrent.ConcurrentHashMap flows;
    java.util.concurrent.ConcurrentHashMap stickyFlows;
}

# ===========================================================
# MviViewModel 节流缓存
# ===========================================================
-keepclassmembers class com.answufeng.arch.mvi.MviViewModel {
    java.util.Map intentThrottleMap;
}
