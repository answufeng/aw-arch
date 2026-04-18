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
 * ViewBinding 委托，自动管理 Fragment/Activity 的 ViewBinding 生命周期。
 *
 * Fragment 用法（自动在 onDestroyView 时置空）：
 * ```kotlin
 * class MyFragment : Fragment() {
 *     private val binding by viewBinding(MyFragmentBinding::inflate)
 * }
 * ```
 *
 * Activity 用法（自动 setContentView）：
 * ```kotlin
 * class MyActivity : AppCompatActivity() {
 *     private val binding by viewBinding(MyActivityBinding::inflate)
 * }
 * ```
 */

/** Fragment ViewBinding 委托，View 销毁后自动释放 */
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
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            throw IllegalStateException("Cannot access view bindings after view has been destroyed")
        }

        val view = fragment.view
            ?: throw IllegalStateException("Cannot access view bindings when Fragment.view is null")

        val viewBinding = inflate(
            LayoutInflater.from(fragment.requireContext()),
            view.parent as? ViewGroup,
            false
        )
        this.binding = viewBinding
        return viewBinding
    }
}

/** Activity ViewBinding 委托，首次访问时自动 setContentView */
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
