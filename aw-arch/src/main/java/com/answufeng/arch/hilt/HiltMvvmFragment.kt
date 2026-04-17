package com.answufeng.arch.hilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UIEvent
import kotlinx.coroutines.launch

abstract class HiltMvvmFragment<VB : ViewBinding, VM : MvvmViewModel> : Fragment() {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreateView or after onDestroyView")

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract val viewModel: VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    protected open fun navigateTo(route: String, extras: Map<String, Any>? = null) {}

    protected open fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    protected open fun handleCustomEvent(key: String, data: Any?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
