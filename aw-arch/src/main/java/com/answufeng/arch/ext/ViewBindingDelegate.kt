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

/**
 * Fragment ViewBinding 委托。
 *
 * 在 Fragment 中使用：
 * ```kotlin
 * private val binding by viewBinding<FragmentHomeBinding>()
 * ```
 *
 * 会在 Fragment 的 `onViewCreated` 时自动初始化，
 * 在 `onDestroyView` 时自动清理，避免内存泄漏。
 */
inline fun <reified VB : ViewBinding> Fragment.viewBinding() =
    FragmentViewBindingDelegate(VB::class.java, this)

/**
 * Fragment ViewBinding 委托实现。
 */
class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val bindingClass: Class<VB>,
    private val fragment: Fragment
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

        val method = bindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        val viewBinding = method.invoke(null, LayoutInflater.from(fragment.context), fragment.view?.parent as ViewGroup?, false) as VB
        this.binding = viewBinding
        return viewBinding
    }
}

/**
 * Activity ViewBinding 委托。
 *
 * 在 Activity 中使用：
 * ```kotlin
 * private val binding by viewBinding<ActivityMainBinding>()
 * ```
 */
inline fun <reified VB : ViewBinding> android.app.Activity.viewBinding() =
    ActivityViewBindingDelegate(VB::class.java, this)

/**
 * Activity ViewBinding 委托实现。
 */
class ActivityViewBindingDelegate<VB : ViewBinding>(
    private val bindingClass: Class<VB>,
    private val activity: android.app.Activity
) : ReadOnlyProperty<android.app.Activity, VB> {

    private var binding: VB? = null

    override fun getValue(thisRef: android.app.Activity, property: KProperty<*>): VB {
        val binding = binding
        if (binding != null) return binding

        val method = bindingClass.getMethod("inflate", LayoutInflater::class.java)
        val viewBinding = method.invoke(null, LayoutInflater.from(activity)) as VB
        activity.setContentView(viewBinding.root)
        this.binding = viewBinding
        return viewBinding
    }
}