package com.answufeng.arch.mvp

import com.answufeng.arch.config.AwArch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVP Presenter 基类：
 * - 维护 View 引用（attach/detach）
 * - 内置协程作用域，detach 时自动 cancel，避免泄漏
 */
abstract class BaseMvpPresenter<V : MvpView> : MvpPresenter<V> {

    private var _view: V? = null
    protected val viewOrNull: V? get() = _view
    protected val view: V get() = _view ?: error("View is not attached yet (or already detached).")

    override val isViewAttached: Boolean get() = _view != null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AwArch.logger.e(this::class.simpleName ?: "BaseMvpPresenter", "Unhandled exception", throwable)
        onUnhandledException(throwable)
    }

    private var job = SupervisorJob()

    /** Presenter 级协程作用域；每次 attach 会重建，detach 时 cancel。*/
    protected var presenterScope: CoroutineScope =
        CoroutineScope(Dispatchers.Main.immediate + job + exceptionHandler)
        private set

    override fun attachView(view: V) {
        _view = view
        if (job.isCancelled) {
            presenterScope.cancel()
            job = SupervisorJob()
        }
        presenterScope = CoroutineScope(Dispatchers.Main.immediate + job + exceptionHandler)
        onViewAttached(view)
    }

    override fun detachView() {
        val old = _view
        _view = null
        presenterScope.cancel()
        onViewDetached(old)
    }

    protected open fun onViewAttached(view: V) {}
    protected open fun onViewDetached(view: V?) {}

    protected open fun onUnhandledException(throwable: Throwable) {}

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        presenterScope.launch(block = block)
    }

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        presenterScope.launch(context = Dispatchers.IO, block = block)
    }

    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit) {
        presenterScope.launch(context = Dispatchers.Default, block = block)
    }

    protected suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Main, block)
}

