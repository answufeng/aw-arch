package com.answufeng.arch.mvvm

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.answufeng.arch.base.BaseActivity
import com.answufeng.arch.base.MvvmViewModel

abstract class MvvmActivity<VB : ViewBinding, VM : MvvmViewModel> : BaseActivity<VB>(), MvvmView<VM> {

    private var _viewModel: VM? = null

    override val viewModel: VM
        get() = _viewModel ?: error("ViewModel not initialized. Ensure onPreInit is called.")

    override val viewContext: Context get() = this

    abstract fun viewModelClass(): Class<VM>

    protected open fun createViewModel(): VM {
        return ViewModelProvider(this)[viewModelClass()]
    }

    override fun onPreInit(savedInstanceState: Bundle?) {
        _viewModel = createViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectUIEvents()
    }

    override fun onNavigateBack() {
        finish()
    }
}
