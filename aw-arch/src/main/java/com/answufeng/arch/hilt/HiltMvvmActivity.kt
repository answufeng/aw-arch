package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UIEvent
import kotlinx.coroutines.launch

abstract class HiltMvvmActivity<VB : ViewBinding, VM : MvvmViewModel> : AppCompatActivity() {

    protected lateinit var binding: VB

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { onUIEvent(it) }
            }
        }
    }

    abstract val viewModel: VM

    open fun onUIEvent(event: UIEvent) {
        when (event) {
            is UIEvent.Toast -> showToast(event.message)
            is UIEvent.Loading -> onLoading(event.show)
            is UIEvent.Navigate -> navigateTo(event.route, event.extras)
            is UIEvent.NavigateBack -> navigateBack()
            is UIEvent.Custom -> handleCustomEvent(event.key, event.data)
        }
    }

    open fun onLoading(show: Boolean) {}

    protected open fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    protected open fun navigateTo(route: String, extras: Map<String, Any>? = null) {}

    protected open fun navigateBack() {
        finish()
    }

    protected open fun handleCustomEvent(key: String, data: Any?) {}
}
