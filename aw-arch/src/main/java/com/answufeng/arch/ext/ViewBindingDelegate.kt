package com.answufeng.arch.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified VB : ViewBinding> Fragment.viewBinding(
    noinline inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) = FragmentViewBindingDelegate(this, inflate)

class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val fragment: Fragment,
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : ReadOnlyProperty<Fragment, VB> {

    private var binding: VB? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        val binding = binding
        if (binding != null) return binding

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Cannot access view bindings when view lifecycle is not initialized")
        }

        val viewBinding = inflate(
            LayoutInflater.from(fragment.requireContext()),
            fragment.view?.parent as? ViewGroup,
            false
        )
        this.binding = viewBinding
        return viewBinding
    }
}

inline fun <reified VB : ViewBinding> android.app.Activity.viewBinding(
    noinline inflate: (LayoutInflater) -> VB
) = ActivityViewBindingDelegate(this, inflate)

class ActivityViewBindingDelegate<VB : ViewBinding>(
    private val activity: android.app.Activity,
    private val inflate: (LayoutInflater) -> VB
) : ReadOnlyProperty<android.app.Activity, VB> {

    private var binding: VB? = null

    override fun getValue(thisRef: android.app.Activity, property: KProperty<*>): VB {
        val binding = binding
        if (binding != null) return binding

        val viewBinding = inflate(LayoutInflater.from(activity))
        activity.setContentView(viewBinding.root)
        this.binding = viewBinding
        return viewBinding
    }
}
