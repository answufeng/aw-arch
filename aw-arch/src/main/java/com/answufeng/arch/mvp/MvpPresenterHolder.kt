package com.answufeng.arch.mvp

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.answufeng.arch.ext.inferPresenterClass

/**
 * 在 [Fragment] 配置变更与 View 重建之间保留 Presenter 实例。
 */
internal class MvpPresenterHolder<P : Any>(val presenter: P) : ViewModel()

internal class MvpPresenterViewModelFactory<P : Any>(
    private val creator: () -> P,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MvpPresenterHolder::class.java)) {
            return MvpPresenterHolder(creator()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

/**
 * 通过反射创建 Presenter；失败时抛出带 R8 提示的 [IllegalStateException]。
 */
@SuppressLint("DiscouragedPrivateApi")
internal fun <P : MvpPresenter<*>> reflectiveCreatePresenter(ownerClass: Class<*>): P {
    val pClass = inferPresenterClass<P>(ownerClass, MvpPresenter::class.java)
    return try {
        val ctor = pClass.getDeclaredConstructor()
        ctor.isAccessible = true
        ctor.newInstance()
    } catch (e: ReflectiveOperationException) {
        throw IllegalStateException(
            "Cannot reflectively instantiate ${pClass.name}; override createPresenter() or adjust R8 / API restrictions.",
            e,
        )
    }
}
