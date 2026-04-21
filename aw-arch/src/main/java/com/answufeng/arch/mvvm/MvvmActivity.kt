package com.answufeng.arch.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.base.MvvmViewModel.UiEvent
import com.answufeng.arch.ext.inferViewModelClass
import kotlinx.coroutines.launch

abstract class MvvmActivity<VB : ViewBinding, VM : MvvmViewModel> : AppCompatActivity(), MvvmView {

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding ?: error("ViewBinding is not available before onCreate or after onDestroy")

    protected lateinit var viewModel: VM

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel()
        initView(savedInstanceState)
        initObservers()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.UiEvent.collect { onUiEvent(it) }
            }
        }
    }

    protected open fun createViewModel(): VM {
        val vmClass = inferViewModelClass(javaClass, MvvmViewModel::class.java)
        return ViewModelProvider(this)[vmClass]
    }

    override fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
