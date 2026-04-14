package com.answufeng.arch.mvvm

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.base.MvvmViewModel
import kotlinx.coroutines.launch

interface MvvmView<VM : MvvmViewModel> : LifecycleOwner {

    val viewModel: VM

    fun collectUIEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event -> onUIEvent(event) }
            }
        }
    }

    fun onUIEvent(event: MvvmViewModel.UIEvent) {
        when (event) {
            is MvvmViewModel.UIEvent.Toast -> showToast(event.message)
            is MvvmViewModel.UIEvent.Loading -> onLoading(event.show)
            is MvvmViewModel.UIEvent.NavigateBack -> onNavigateBack()
            is MvvmViewModel.UIEvent.Navigate -> onNavigate(event.route, event.extras)
            is MvvmViewModel.UIEvent.Custom -> onCustomEvent(event.key, event.data)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(viewContext, message, Toast.LENGTH_SHORT).show()
    }

    val viewContext: Context

    fun onLoading(show: Boolean) {}

    fun onNavigate(route: String, extras: Map<String, Any>?) {}

    fun onNavigateBack() {}

    fun onCustomEvent(key: String, data: Any?) {}
}
