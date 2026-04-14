package com.answufeng.arch.ext

import android.view.LayoutInflater
import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified VB : ViewBinding> ComponentActivity.viewBinding(
    crossinline inflate: (LayoutInflater) -> VB
): Lazy<VB> = lazy(LazyThreadSafetyMode.NONE) {
    inflate(layoutInflater)
}

fun <VB : ViewBinding> Fragment.viewBinding(
    bind: (View) -> VB
): ReadOnlyProperty<Fragment, VB> = FragmentViewBindingDelegate(bind)

private class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val bind: (View) -> VB
) : ReadOnlyProperty<Fragment, VB> {

    private var binding: VB? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        binding?.let { return it }

        val viewLifecycleOwner = try {
            thisRef.viewLifecycleOwner
        } catch (_: IllegalStateException) {
            error("ViewBinding cannot be accessed before onCreateView() or after onDestroyView()")
        }

        if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            error("ViewBinding cannot be accessed after onDestroyView()")
        }

        val newBinding = bind(thisRef.requireView())
        binding = newBinding

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                binding = null
            }
        })

        return newBinding
    }
}
