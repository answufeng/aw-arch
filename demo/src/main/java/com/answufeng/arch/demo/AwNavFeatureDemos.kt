package com.answufeng.arch.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.arch.demo.databinding.ActivityAwnNavBasicBinding
import com.answufeng.arch.demo.databinding.ActivityAwnNavInterceptorBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.NavAnim

class AwNavBasicRouteDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnNavBasicBinding
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnNavBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nav = AwNav.init(this, binding.container.id)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
            .register<SettingsFragment>("settings")

        if (savedInstanceState == null) {
            nav.navigate("home") { addToBackStack = false; anim = NavAnim.NONE }
        }

        binding.btnHome.setOnClickListener { nav.navigate("home") { anim = NavAnim.FADE; singleTop = true } }
        binding.btnDetail.setOnClickListener { nav.navigate("detail") { anim = NavAnim.SLIDE_HORIZONTAL } }
        binding.btnSettings.setOnClickListener { nav.navigate("settings") { anim = NavAnim.SLIDE_VERTICAL } }
        binding.btnBack.setOnClickListener { if (!nav.back()) finish() }
    }
}

class AwNavInterceptorDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnNavInterceptorBinding
    private lateinit var nav: AwNav

    /** 为 true 时拦截跳转到 detail，用于演示 [AwNav.addInterceptor] */
    private var blockDetailNavigation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnNavInterceptorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nav = AwNav.init(this, binding.container.id)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
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

        binding.btnHome.setOnClickListener { nav.navigate("home") { anim = NavAnim.FADE; singleTop = true } }
        binding.btnDetail.setOnClickListener { nav.navigate("detail") { anim = NavAnim.SLIDE_HORIZONTAL } }
        binding.btnBack.setOnClickListener { if (!nav.back()) finish() }

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

