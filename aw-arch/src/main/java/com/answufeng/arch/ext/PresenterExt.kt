package com.answufeng.arch.ext

import com.answufeng.arch.mvp.MvpPresenter

/**
 * 通过反射推断继承链上父类泛型参数中的 Presenter 类型。
 *
 * 自 [javaClass] 起沿 [Class.getGenericSuperclass] 向上查找，在每一层的 `ParameterizedType`
 * 实参中找到第一个可赋值为 [baseClass] 的具体类型。
 */
@Suppress("UNCHECKED_CAST")
internal fun <P : MvpPresenter<*>> inferPresenterClass(
    javaClass: Class<*>,
    baseClass: Class<out MvpPresenter<*>>
): Class<P> {
    var current: Class<*>? = javaClass
    while (current != null) {
        val supertype = current.genericSuperclass
        if (supertype is java.lang.reflect.ParameterizedType) {
            for (arg in supertype.actualTypeArguments) {
                resolvePresenterArg(arg, baseClass)?.let { return it as Class<P> }
            }
        }
        current = current.superclass
    }
    throw IllegalStateException("Cannot infer Presenter class. Override createPresenter() or specify generic type parameters.")
}

private fun resolvePresenterArg(
    type: java.lang.reflect.Type,
    baseClass: Class<out MvpPresenter<*>>
): Class<*>? = when (type) {
    is Class<*> -> type.takeIf { baseClass.isAssignableFrom(it) }
    is java.lang.reflect.ParameterizedType -> {
        val raw = type.rawType as? Class<*> ?: return null
        raw.takeIf { baseClass.isAssignableFrom(it) }
    }
    else -> null
}

