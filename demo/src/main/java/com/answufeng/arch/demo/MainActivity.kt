package com.answufeng.arch.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import com.answufeng.arch.state.retryLoadState
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DemoEvent(val message: String)

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 主布局
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)

        // 标题
        mainLayout.addView(TextView(this).apply {
            text = "🏗️ aw-arch 功能演示"
            textSize = 20f
            setPadding(0, 0, 0, 20)
        })

        // 核心功能卡片
        val coreCard = createCard("核心功能")
        val coreLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        coreLayout.addView(createButton("⏳ LoadState 测试", ::testLoadState))
        coreLayout.addView(createButton("🔄 重试机制测试", ::testRetryLoadState))
        coreLayout.addView(createButton("📢 发送事件", ::postEvent))
        coreLayout.addView(createButton("📢 发送粘性事件", ::postStickyEvent))
        coreCard.addView(coreLayout)
        mainLayout.addView(coreCard)

        // 架构模式卡片
        val archCard = createCard("架构模式")
        val archLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        archLayout.addView(createButton("🔷 MVVM 演示", ::openMvvmDemo))
        archLayout.addView(createButton("🔶 MVI 演示", ::openMviDemo))
        archCard.addView(archLayout)
        mainLayout.addView(archCard)

        // 导航功能卡片
        val navCard = createCard("导航功能")
        val navLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        navLayout.addView(createButton("🧭 基础导航", ::openNavDemo))
        navLayout.addView(createButton("💬 微信风格导航", ::openWeChatDemo))
        navCard.addView(navLayout)
        mainLayout.addView(navCard)

        // 管理功能卡片
        val manageCard = createCard("管理功能")
        val manageLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        manageLayout.addView(createButton("🗑️ 清除日志", ::clearLog))
        manageCard.addView(manageLayout)
        mainLayout.addView(manageCard)

        // 日志区域
        mainLayout.addView(TextView(this).apply {
            text = "操作日志："
            textSize = 16f
            setPadding(0, 20, 0, 10)
        })

        val logScrollView = ScrollView(this)
        tvLog = TextView(this).apply {
            text = "操作日志将显示在这里..."
            setPadding(10, 10, 10, 10)
        }
        logScrollView.addView(tvLog)
        mainLayout.addView(logScrollView)

        // 监听事件
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("✅ 收到事件: ${event.message}")
        }

        log("✅ aw-arch 初始化完成")
        log("📊 点击按钮测试各项功能")
    }

    private fun createCard(title: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setPadding(20, 20, 20, 20)

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                setPadding(0, 0, 0, 12)
            })
        }
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 4)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
        val scrollView = tvLog.parent as? ScrollView
        scrollView?.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun clearLog() {
        tvLog.text = "日志已清除\n"
    }

    private fun testLoadState() {
        lifecycleScope.launch {
            log("🔄 开始测试 LoadState...")
            val state: LoadState<String> = loadStateCatching {
                delay(1000)
                "数据加载成功！"
            }
            when (state) {
                is LoadState.Loading -> log("⏳ 加载中...")
                is LoadState.Success -> log("✅ 成功: ${state.data}")
                is LoadState.Error -> log("❌ 错误: ${state.message}")
            }
        }
    }

    private fun testRetryLoadState() {
        lifecycleScope.launch {
            log("🔄 开始测试重试机制（3次重试）...")
            var attempt = 0
            val state = retryLoadState(times = 3, initialDelayMillis = 500) {
                attempt++
                if (attempt < 3) {
                    log("⏳ 第${attempt}次尝试失败")
                    throw RuntimeException("第${attempt}次尝试失败")
                }
                log("✅ 第${attempt}次尝试成功")
                "在第${attempt}次尝试时恢复"
            }
            when (state) {
                is LoadState.Success -> log("✅ 最终成功: ${state.data}")
                is LoadState.Error -> log("❌ 最终失败: ${state.message}")
                is LoadState.Loading -> log("⏳ 加载中...")
            }
        }
    }

    private fun postEvent() {
        lifecycleScope.launch {
            val event = DemoEvent("Hello from FlowEventBus!")
            FlowEventBus.post(event)
            log("📢 发送事件(suspend): ${event.message}")
        }
    }

    private fun postStickyEvent() {
        lifecycleScope.launch {
            val event = DemoEvent("Sticky event!")
            FlowEventBus.postSticky(event)
            log("📢 发送粘性事件: ${event.message}")
        }
    }

    private fun openMvvmDemo() {
        log("🚀 打开 MVVM 演示")
        startActivity(android.content.Intent(this, MvvmDemoActivity::class.java))
    }

    private fun openMviDemo() {
        log("🚀 打开 MVI 演示")
        startActivity(android.content.Intent(this, MviDemoActivity::class.java))
    }

    private fun openNavDemo() {
        log("🚀 打开导航演示")
        startActivity(android.content.Intent(this, NavDemoActivity::class.java))
    }

    private fun openWeChatDemo() {
        log("🚀 打开微信演示")
        startActivity(android.content.Intent(this, com.answufeng.arch.demo.wechat.WeChatActivity::class.java))
    }
}
