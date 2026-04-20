package com.answufeng.arch.ext

import androidx.lifecycle.ViewModel

/**
 * 通过反射推断父类泛型参数中的 [ViewModel] 类型。
 *
 * 遍历 [javaClass] 的父类泛型参数，找到第一个 [baseClass] 的子类并返回其 [Class] 对象。
 * 当无法推断时抛出 [IllegalStateException]，此时需要调用方覆写 `createViewModel()` 或显式指定泛型参数。
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
    val superclass = javaClass.genericSuperclass
    if (superclass is java.lang.reflect.ParameterizedType) {
        val types = superclass.actualTypeArguments
        for (type in types) {
            if (type is Class<*> && baseClass.isAssignableFrom(type)) {
                return type as Class<VM>
            }
        }
    }
    throw IllegalStateException("Cannot infer ViewModel class. Override createViewModel() or specify generic type parameters.")
}
