package com.answufeng.arch.config

import com.answufeng.arch.event.FlowEventBus

@DslMarker
annotation class AwArchDsl

/**
 * aw-arch 全局配置入口。
 *
 * 在 Application.onCreate() 中初始化：
 * ```kotlin
 * AwArch.init {
 *     logger = TimberAwLogger()
 *     strictMainThreadForAwNav = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
 *     flowEventBusAutoCleanupDelayMs = 60_000L
 * }
 * ```
 */
@AwArchDsl
object AwArch {

    /** 全局日志实现，默认使用 Android Log（测试环境自动降级为 println） */
    @Volatile
    var logger: AwLogger = DefaultAwLogger()

    /**
     * 为 `true` 时，[com.answufeng.arch.nav.AwNav] 的 `navigate` / `back` / `backTo` / `clearStack`
     * 在非主线程调用会抛出 [IllegalStateException]。建议在 debug 构建开启。
     */
    @Volatile
    var strictMainThreadForAwNav: Boolean = false

    /**
     * 为 `true` 时，因防连点节流而忽略的 [com.answufeng.arch.nav.AwNav.navigate] 会打一条 [AwLogger.d] 日志。
     */
    @Volatile
    var logAwNavThrottledNavigations: Boolean = false

    /**
     * 若设置，[init] 结束时同步到 [FlowEventBus.autoCleanupDelay]（毫秒）。
     * 未设置则保持 [FlowEventBus] 现有默认值。
     */
    @Volatile
    var flowEventBusAutoCleanupDelayMs: Long? = null

    /**
     * DSL 方式初始化配置。
     */
    fun init(block: AwArch.() -> Unit) {
        block()
        flowEventBusAutoCleanupDelayMs?.let { FlowEventBus.autoCleanupDelay = it }
    }
}

/**
 * aw-arch 日志接口，可自定义实现（如 Timber）。
 *
 * ```kotlin
 * class TimberAwLogger : AwLogger {
 *     override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
 *     override fun w(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).w(throwable, message)
 *     override fun e(tag: String, message: String, throwable: Throwable?) = Timber.tag(tag).e(throwable, message)
 * }
 * ```
 */
interface AwLogger {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * 默认日志实现。
 *
 * Android 环境使用 [android.util.Log]；无法判定为可用 Android 日志 API 时降级为 [System.out]。
 *
 * 通过尝试调用 [android.util.Log.isLoggable] 探测运行环境（部分桩/精简环境会抛异常，此时走标准输出）。
 */
internal class DefaultAwLogger : AwLogger {

    private val isAndroid: Boolean by lazy {
        try {
            android.util.Log.isLoggable("AwArch", android.util.Log.VERBOSE)
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
