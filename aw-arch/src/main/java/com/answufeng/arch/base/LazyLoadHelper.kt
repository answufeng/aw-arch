package com.answufeng.arch.base

import android.os.Bundle
import androidx.fragment.app.Fragment

class LazyLoadHelper(private val fragment: Fragment) {

    private var isFirstLoad = true

    companion object {
        private const val KEY_IS_FIRST_LOAD = "aw_lazy_load_is_first_load"
    }

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isFirstLoad = savedInstanceState.getBoolean(KEY_IS_FIRST_LOAD, true)
        }
    }

    fun onResume() {
        if (isFirstLoad) {
            isFirstLoad = false
        }
    }

    fun shouldLazyLoad(): Boolean {
        if (isFirstLoad) {
            isFirstLoad = false
            return true
        }
        return false
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_FIRST_LOAD, isFirstLoad)
    }
}
