package com.answufeng.arch.demo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topBar))

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
        findViewById<Button>(R.id.btnHilt).setOnClickListener { openHiltDemo() }
        findViewById<Button>(R.id.btnSimpleMvi).setOnClickListener { openSimpleMviDemo() }
        findViewById<Button>(R.id.btnRemoveSticky).setOnClickListener { removeStickyDemo() }
        findViewById<Button>(R.id.btnClearLog).setOnClickListener { clearLog() }

        findViewById<View>(R.id.btnPlaybook).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.demo_playbook_title)
                .setMessage(R.string.demo_playbook_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[事件] ${event.message}")
        }

        log("[就绪] aw-arch Demo")
        log("[提示] 点击下方区块按钮逐项验证；FlowEventBus 自动清理见 README")
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
    }

    private fun clearLog() {
        tvLog.text = "日志已清除\n"
    }

    private fun testLoadState() {
        lifecycleScope.launch {
            log("[LoadState] 开始加载…")
            val state: LoadState<String> = loadStateCatching {
                delay(1000)
                "数据加载成功！"
            }
            when (state) {
                is LoadState.Loading -> log("[LoadState] 加载中…")
                is LoadState.Success -> log("[LoadState] 成功: ${state.data}")
                is LoadState.Error -> log("[LoadState] 错误: ${state.message}")
            }
        }
    }

    private fun testRetryLoadState() {
        lifecycleScope.launch {
            log("[Retry] 开始（最多 3 次重试）…")
            var attempt = 0
            val state = retryLoadState(times = 3, initialDelayMillis = 500) {
                attempt++
                if (attempt < 3) {
                    log("[Retry] 第 ${attempt} 次失败")
                    throw RuntimeException("第${attempt}次尝试失败")
                }
                log("[Retry] 第 ${attempt} 次成功")
                "在第${attempt}次尝试时恢复"
            }
            when (state) {
                is LoadState.Success -> log("[Retry] 最终成功: ${state.data}")
                is LoadState.Error -> log("[Retry] 最终失败: ${state.message}")
                is LoadState.Loading -> log("[Retry] 加载中…")
            }
        }
    }

    private fun postEvent() {
        lifecycleScope.launch {
            val event = DemoEvent("Hello from FlowEventBus!")
            FlowEventBus.post(event)
            log("[Bus] post: ${event.message}")
        }
    }

    private fun tryPostEvent() {
        val event = DemoEvent("Hello from tryPost!")
        val result = FlowEventBus.tryPost(event)
        log("[Bus] tryPost: ${event.message}, ok=$result")
    }

    private fun postStickyEvent() {
        lifecycleScope.launch {
            val event = DemoEvent("Sticky event!")
            FlowEventBus.postSticky(event)
            log("[Bus] sticky: ${event.message}")
        }
    }

    private fun openMvvmDemo() {
        log("[打开] MVVM")
        startActivity(android.content.Intent(this, MvvmDemoActivity::class.java))
    }

    private fun openMviDemo() {
        log("[打开] MVI")
        startActivity(android.content.Intent(this, MviDemoActivity::class.java))
    }

    private fun openNavDemo() {
        log("[打开] AwNav")
        startActivity(android.content.Intent(this, NavDemoActivity::class.java))
    }

    private fun openWeChatDemo() {
        log("[打开] 微信式 Demo")
        startActivity(android.content.Intent(this, com.answufeng.arch.demo.wechat.WeChatActivity::class.java))
    }

    private fun openHiltDemo() {
        log("[打开] Hilt")
        startActivity(android.content.Intent(this, HiltDemoActivity::class.java))
    }

    private fun openSimpleMviDemo() {
        log("[打开] SimpleMVI")
        startActivity(android.content.Intent(this, SimpleMviDemoActivity::class.java))
    }

    private fun removeStickyDemo() {
        FlowEventBus.removeSticky<DemoEvent>()
        log("[Bus] removeSticky<DemoEvent>")
    }
}
