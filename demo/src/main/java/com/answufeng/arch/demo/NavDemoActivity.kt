package com.answufeng.arch.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.databinding.ActivityNavDemoBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.NavAnim

class NavDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavDemoBinding
    private lateinit var nav: AwNav

    /** 为 true 时拦截跳转到 detail，用于演示 [AwNav.addInterceptor] */
    private var blockDetailNavigation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nav = AwNav.init(this, binding.container.id)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
            .register<SettingsFragment>("settings")
            .addInterceptor { _, to, _ ->
                if (blockDetailNavigation && to == "detail") {
                    binding.tvInterceptStatus.text = "拦截器已阻止：detail（切换下方开关可放行）"
                    false
                } else {
                    true
                }
            }

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

        binding.btnToggleIntercept.setOnClickListener {
            blockDetailNavigation = !blockDetailNavigation
            binding.tvInterceptStatus.text = if (blockDetailNavigation) {
                "拦截 Detail：开启"
            } else {
                "拦截 Detail：关闭（可进入 detail）"
            }
        }
    }
}

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
