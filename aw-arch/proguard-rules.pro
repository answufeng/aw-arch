# aw-arch ProGuard Rules
# 此文件用于库自身的 release 构建混淆规则
# Consumer-facing rules（供使用者混淆时使用）位于 consumer-rules.pro

# ===========================================================
# 保留公共 API 和 View 构造函数
# ===========================================================

# 保留所有公共类
-keep class com.answufeng.arch.** { *; }

# 保留 Hilt 相关类
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# 保留 Lifecycle 相关类
-keep class androidx.lifecycle.** { *; }

# ===========================================================
# 保留 Kotlin 反射和元数据
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

# 保留 Kotlin Metadata
-keep class kotlin.Metadata { *; }

# ===========================================================
# 保留 View 构造函数（自定义 View 必须在 XML 中能正常实例化）
# ===========================================================

# 保留所有 View 的无参构造函数
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ===========================================================
# 保留枚举和 sealed class
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
