package com.answufeng.arch.ext

import androidx.lifecycle.ViewModel

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
