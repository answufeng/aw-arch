package com.answufeng.arch.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import com.answufeng.arch.state.retryLoadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DemoEvent(val message: String)

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)

        findViewById<Button>(R.id.btnLoadState).setOnClickListener { testLoadState() }
        findViewById<Button>(R.id.btnRetryLoadState).setOnClickListener { testRetryLoadState() }
        findViewById<Button>(R.id.btnPostEvent).setOnClickListener { postEvent() }
        findViewById<Button>(R.id.btnTryPostEvent).setOnClickListener { tryPostEvent() }
        findViewById<Button>(R.id.btnStickyEvent).setOnClickListener { postStickyEvent() }
        findViewById<Button>(R.id.btnMvvm).setOnClickListener { openMvvmDemo() }
        findViewById<Button>(R.id.btnMvi).setOnClickListener { openMviDemo() }
        findViewById<Button>(R.id.btnNav).setOnClickListener { openNavDemo() }
        findViewById<Button>(R.id.btnWeChat).setOnClickListener { openWeChatDemo() }
        findViewById<Button>(R.id.btnClearLog).setOnClickListener { clearLog() }

        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("✅ 收到事件: ${event.message}")
        }

        log("✅ aw-arch 初始化完成")
        log("📊 点击按钮测试各项功能")
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
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

    private fun tryPostEvent() {
        val event = DemoEvent("Hello from tryPost!")
        val result = FlowEventBus.tryPost(event)
        log("📢 发送事件(tryPost): ${event.message}, 结果: $result")
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
