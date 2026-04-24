package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.retryLoadState
import kotlinx.coroutines.launch

class RetryLoadStateDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "RetryLoadState"
    override val pageDesc: String = "演示 retryLoadState：失败自动重试（指数退避），最终返回 Success/Error。"

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("开始重试（最多 3 次）") { runDemo() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun runDemo() {
        lifecycleScope.launch {
            log("[Retry] 开始（最多 3 次重试）…")
            var attempt = 0
            val state = retryLoadState(times = 3, initialDelayMillis = 300) {
                attempt++
                if (attempt < 3) {
                    log("[Retry] 第 $attempt 次失败")
                    throw RuntimeException("第${attempt}次尝试失败")
                }
                log("[Retry] 第 $attempt 次成功")
                "在第${attempt}次尝试时恢复"
            }
            when (state) {
                is LoadState.Success -> log("[Retry] 最终成功: ${state.data}")
                is LoadState.Error -> log("[Retry] 最终失败: ${state.message}")
                is LoadState.Loading -> log("[Retry] 加载中…")
            }
        }
    }
}

