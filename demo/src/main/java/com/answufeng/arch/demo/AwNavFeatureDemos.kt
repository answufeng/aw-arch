package com.answufeng.arch.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.demo.databinding.ActivityAwnNavBasicBinding
import com.answufeng.arch.demo.databinding.ActivityAwnNavInterceptorBinding
import com.answufeng.arch.demo.databinding.ActivityAwnavTabStackBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.NavAnim
import com.answufeng.arch.nav.TabConfig
import kotlinx.coroutines.launch

class AwNavBasicRouteDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnNavBasicBinding
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnNavBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        nav = AwNav.init(this, binding.container.id, savedInstanceState = savedInstanceState)
            .registerAnnotated<HomeFragment>()
            .registerAnnotated<DetailFragment>()
            .registerAnnotated<SettingsFragment>()

        if (savedInstanceState == null) {
            nav.navigate("home") { anim = NavAnim.NONE }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                nav.currentRouteFlow.collect { route ->
                    binding.tvNavStatus.text =
                        "当前路由: ${route ?: "-"}  |  栈深: ${nav.stackDepth}"
                }
            }
        }

        // 默认动画：左右滑动（最常用，无需显式指定）
        binding.btnHome.setOnClickListener {
            nav.navigate("home") { singleTop = true; anim = NavAnim.FADE }
        }
        binding.btnDetail.setOnClickListener {
            nav.navigate("detail") // 默认 SLIDE_HORIZONTAL
        }
        binding.btnSettings.setOnClickListener {
            nav.navigate("settings") { anim = NavAnim.SLIDE_VERTICAL }
        }
        binding.btnBack.setOnClickListener {
            if (!nav.back()) finish()
        }
        binding.btnBackTo.setOnClickListener {
            if (!nav.backTo("home")) {
                Toast.makeText(this, "返回栈中没有 home", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnClearStack.setOnClickListener {
            nav.clearStack()
            Toast.makeText(this, "返回栈已清空", Toast.LENGTH_SHORT).show()
        }
        binding.btnSingleTop.setOnClickListener {
            nav.navigate("detail") {
                singleTop = true
                onSingleTopReuse = {
                    Toast.makeText(
                        this@AwNavBasicRouteDemoActivity,
                        "singleTop: 复用了现有 DetailFragment",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nav.saveState(outState)
    }
}

class AwNavTabStackDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnavTabStackBinding
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnavTabStackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        nav = AwNav.init(this, binding.container.id, savedInstanceState = savedInstanceState)
            .registerAnnotated<HomeFragment>()
            .registerAnnotated<DetailFragment>()
            .registerAnnotated<SettingsFragment>()

        nav.initTabs(
            TabConfig("a", rootRoute = "home", switchAnim = NavAnim.FADE),
            TabConfig("b", rootRoute = "home", switchAnim = NavAnim.FADE),
            TabConfig("c", rootRoute = "home", switchAnim = NavAnim.FADE),
        )

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.tab_a
            nav.switchTab("a")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                nav.currentRouteFlow.collect { route ->
                    binding.tvTabStatus.text =
                        getString(
                            R.string.demo_awnav_tab_status,
                            nav.currentTabId ?: "-",
                            route ?: "-",
                            nav.stackDepth,
                        )
                }
            }
        }

        binding.btnPushDetail.setOnClickListener {
            nav.navigate("detail", bundleOf("key" to "from tab ${nav.currentTabId}"))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val tabId =
                when (item.itemId) {
                    R.id.tab_a -> "a"
                    R.id.tab_b -> "b"
                    R.id.tab_c -> "c"
                    else -> return@setOnItemSelectedListener false
                }
            nav.switchTab(tabId)
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nav.saveState(outState)
    }
}

class AwNavInterceptorDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnNavInterceptorBinding
    private lateinit var nav: AwNav

    private var blockDetailNavigation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnNavInterceptorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        nav = AwNav.init(this, binding.container.id, savedInstanceState = savedInstanceState)
            .registerAnnotated<HomeFragment>()
            .registerAnnotated<DetailFragment>()
            .addInterceptor { _, to, _ ->
                if (blockDetailNavigation && to == "detail") {
                    binding.tvInterceptStatus.setText(R.string.demo_awnav_intercept_blocked)
                    false
                } else {
                    true
                }
            }

        if (savedInstanceState == null) {
            nav.navigate("home") { anim = NavAnim.NONE }
        }

        binding.btnHome.setOnClickListener { nav.navigate("home") { singleTop = true; anim = NavAnim.FADE } }
        binding.btnDetail.setOnClickListener { nav.navigate("detail") }
        binding.btnBack.setOnClickListener { if (!nav.back()) finish() }

        binding.btnToggleIntercept.setOnClickListener {
            blockDetailNavigation = !blockDetailNavigation
            binding.tvInterceptStatus.setText(
                if (blockDetailNavigation) {
                    R.string.demo_awnav_intercept_on
                } else {
                    R.string.demo_awnav_intercept_off
                },
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nav.saveState(outState)
    }
}
