package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.answufeng.arch.nav.AwNav

// NavDemoActivity 已拆分为两个 Feature Activity（见 AwNavFeatureDemos.kt），保留 Fragment 以复用。

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "Home Fragment\nStack: ${AwNav.from(this@HomeFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}

class DetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = arguments?.getString("key") ?: "No args"
        return TextView(requireContext()).apply {
            text = "Detail Fragment\nArgs: $args\nStack: ${AwNav.from(this@DetailFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "Settings Fragment\nStack: ${AwNav.from(this@SettingsFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}
