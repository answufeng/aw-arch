package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.ActivityWechatBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.NavAnim

class WeChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWechatBinding
    private lateinit var nav: AwNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWechatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "微信"

        // 初始化 AwNav
        nav = AwNav.init(this, R.id.container)
            .register<WeChatFragment>("wechat")
            .register<ContactFragment>("contact")
            .register<DiscoverFragment>("discover")
            .register<ProfileFragment>("profile")

        // 初始化底部导航
        setupBottomNavigation()

        // 默认显示微信页面
        if (savedInstanceState == null) {
            nav.navigate("wechat") { addToBackStack = false; anim = NavAnim.NONE }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_wechat -> nav.navigate("wechat") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_contact -> nav.navigate("contact") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_discover -> nav.navigate("discover") { addToBackStack = false; anim = NavAnim.NONE }
                R.id.tab_profile -> nav.navigate("profile") { addToBackStack = false; anim = NavAnim.NONE }
            }
            true
        }
    }
}
