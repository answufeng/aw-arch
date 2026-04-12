package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.databinding.ActivityNavDemoBinding
import com.answufeng.arch.nav.BrickNav
import com.answufeng.arch.nav.NavAnim

class NavDemoActivity : androidx.appcompat.app.AppCompatActivity() {

    private lateinit var nav: BrickNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNavDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nav = BrickNav.init(this, binding.container.id)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
            .register<SettingsFragment>("settings")

        if (savedInstanceState == null) {
            nav.navigate("home") { addToBackStack = false; anim = NavAnim.NONE }
        }

        binding.btnHome.setOnClickListener {
            nav.navigate("home") { anim = NavAnim.FADE; singleTop = true }
        }
        binding.btnDetail.setOnClickListener {
            nav.navigate("detail") { anim = NavAnim.SLIDE_HORIZONTAL }
        }
        binding.btnSettings.setOnClickListener {
            nav.navigate("settings") { anim = NavAnim.SLIDE_VERTICAL }
        }
        binding.btnBack.setOnClickListener {
            if (!nav.back()) finish()
        }
    }
}

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "Home Fragment\nStack: ${BrickNav.from(this@HomeFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}

class DetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = arguments?.getString("key") ?: "No args"
        return TextView(requireContext()).apply {
            text = "Detail Fragment\nArgs: $args\nStack: ${BrickNav.from(this@DetailFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return TextView(requireContext()).apply {
            text = "Settings Fragment\nStack: ${BrickNav.from(this@SettingsFragment).stackDepth}"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
    }
}
