package com.answufeng.arch.config

/**
 * brick-arch 全局配置入口。
 *
 * 在 Application.onCreate() 中初始化：
 * ```kotlin
 * BrickArch.init {
 *     logger = TimberBrickLogger()
 * }
 * ```
 */
object BrickArch {

    /** 全局日志实现，默认使用 Android Log（测试环境自动降级为 println） */
    var logger: BrickLogger = DefaultBrickLogger()

    /**
     * DSL 方式初始化配置。
     */
    fun init(block: BrickArch.() -> Unit) {
        block()
    }
}

/**
 * brick-arch 日志接口，可自定义实现（如 Timber）。
 *
 * ```kotlin
 * class TimberBrickLogger : BrickLogger {
 *     override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
 *     override fun w(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).w(throwable, message)
 *     override fun e(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).e(throwable, message)
 * }
 * ```
 */
interface BrickLogger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * 默认日志实现。
 *
 * Android 环境使用 [android.util.Log]；
 * 非 Android 环境（如 JVM 单元测试）降级为 [System.out]。
 *
 * 通过尝试调用 [android.util.Log.isLoggable] 来检测运行环境，
 * 因为 Android 单元测试中 android.jar 的方法默认抛出 RuntimeException，
 * 而 Robolectric 环境下可以正常调用。
 */
internal class DefaultBrickLogger : BrickLogger {

    private val isAndroid: Boolean by lazy {
        try {
            android.util.Log.isLoggable("BrickArch", android.util.Log.VERBOSE)
            true
        } catch (_: Throwable) {
            false
        }
    }

    override fun d(tag: String, message: String) {
        if (isAndroid) {
            android.util.Log.d(tag, message)
        } else {
            println("[DEBUG][$tag] $message")
        }
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (isAndroid) {
            android.util.Log.w(tag, message, throwable)
        } else {
            println("[WARN][$tag] $message ${throwable?.stackTraceToString()?.orEmpty()}")
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (isAndroid) {
            android.util.Log.e(tag, message, throwable)
        } else {
            println("[ERROR][$tag] $message ${throwable?.stackTraceToString()?.orEmpty()}")
        }
    }
}
