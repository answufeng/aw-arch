package com.answufeng.arch.ext

import androidx.lifecycle.ViewModel

/**
 * 通过反射推断继承链上父类泛型参数中的 [ViewModel] 类型。
 *
 * 自 [javaClass] 起沿 [Class.getGenericSuperclass] 向上查找，在每一层的 `ParameterizedType`
 * 实参中找到第一个可赋值为 [baseClass] 的具体类型。
 * 当无法推断时抛出 [IllegalStateException]，此时需要调用方覆写 `createViewModel()` 或让直接父类保留 `...<..., VM>` 泛型实参。
 *
 * @param VM 目标 ViewModel 类型
 * @param javaClass 被推断的类的 [Class] 对象
 * @param baseClass ViewModel 基类的 [Class] 对象
 * @return 推断出的 ViewModel 类型
 * @throws IllegalStateException 当无法从泛型参数推断出 ViewModel 类型时
 */
@Suppress("UNCHECKED_CAST")
internal fun <VM : ViewModel> inferViewModelClass(
    javaClass: Class<*>,
    baseClass: Class<out ViewModel>
): Class<VM> {
    var current: Class<*>? = javaClass
    while (current != null) {
        val supertype = current.genericSuperclass
        if (supertype is java.lang.reflect.ParameterizedType) {
            for (arg in supertype.actualTypeArguments) {
                resolveViewModelArg(arg, baseClass)?.let { return it as Class<VM> }
            }
        }
        current = current.superclass
    }
    throw IllegalStateException("Cannot infer ViewModel class. Override createViewModel() or specify generic type parameters.")
}

private fun resolveViewModelArg(
    type: java.lang.reflect.Type,
    baseClass: Class<out ViewModel>
): Class<*>? = when (type) {
    is Class<*> -> type.takeIf { baseClass.isAssignableFrom(it) }
    is java.lang.reflect.ParameterizedType -> {
        val raw = type.rawType as? Class<*> ?: return null
        raw.takeIf { baseClass.isAssignableFrom(it) }
    }
    else -> null
}
