package com.answufeng.arch.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.arch.demo.databinding.ActivityAwnNavBasicBinding
import com.answufeng.arch.demo.databinding.ActivityAwnNavInterceptorBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.AwNavTab
import com.answufeng.arch.nav.NavAnim
import com.answufeng.arch.demo.databinding.ActivityAwnavTabStackBinding

class AwNavBasicRouteDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnNavBasicBinding
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnNavBasicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

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

class AwNavTabStackDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAwnavTabStackBinding
    private lateinit var nav: AwNav
    private lateinit var tabSwitcher: com.answufeng.arch.nav.AwNavTabSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwnavTabStackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        nav =
            AwNav.init(this, binding.container.id).apply {
                register<HomeFragment>("tab_a_home")
                register<DetailFragment>("tab_a_detail")
                register<HomeFragment>("tab_b_home")
                register<DetailFragment>("tab_b_detail")
                register<HomeFragment>("tab_c_home")
                register<DetailFragment>("tab_c_detail")
            }

        tabSwitcher =
            nav.tabSwitcher(
                listOf(
                    AwNavTab(id = "a", rootRoute = "tab_a_home"),
                    AwNavTab(id = "b", rootRoute = "tab_b_home"),
                    AwNavTab(id = "c", rootRoute = "tab_c_home"),
                ),
            )

        supportFragmentManager.addOnBackStackChangedListener { refreshStatus() }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.tab_a
            tabSwitcher.selectTab("a")
        } else {
            tabSwitcher.restoreState(savedInstanceState)
            refreshStatus()
        }

        binding.btnPushDetail.setOnClickListener {
            val tab = tabSwitcher.selectedTabId ?: return@setOnClickListener
            nav.navigate("${tab}_detail", Bundle().apply { putString("key", "from tab $tab") }) {
                anim = NavAnim.SLIDE_HORIZONTAL
            }
            refreshStatus()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val tabId =
                when (item.itemId) {
                    R.id.tab_a -> "a"
                    R.id.tab_b -> "b"
                    R.id.tab_c -> "c"
                    else -> return@setOnItemSelectedListener false
                }
            tabSwitcher.selectTab(tabId)
            refreshStatus()
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tabSwitcher.saveState(outState)
    }

    private fun refreshStatus() {
        binding.tvTabStatus.text =
            getString(
                R.string.demo_awnav_tab_status,
                tabSwitcher.selectedTabId ?: "-",
                nav.currentRoute ?: "-",
                nav.stackDepth,
            )
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
        binding.toolbar.setNavigationOnClickListener { finish() }

        nav = AwNav.init(this, binding.container.id)
            .register<HomeFragment>("home")
            .register<DetailFragment>("detail")
            .addInterceptor { _, to, _ ->
                if (blockDetailNavigation && to == "detail") {
                    binding.tvInterceptStatus.setText(R.string.demo_awnav_intercept_blocked)
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
            binding.tvInterceptStatus.setText(
                if (blockDetailNavigation) {
                    R.string.demo_awnav_intercept_on
                } else {
                    R.string.demo_awnav_intercept_off
                },
            )
        }
    }
}

