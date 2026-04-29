package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import com.answufeng.arch.state.retryLoadState
import kotlinx.coroutines.launch

class LoadStateDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "LoadState"
    override val pageDesc: String = "loadStateCatching 三态管理 + retryLoadState 指数退避重试"

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("loadStateCatching") { runLoadState() }
        addActionButton("retryLoadState（3 次退避）") { runRetryLoadState() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun runLoadState() {
        lifecycleScope.launch {
            log("[LoadState] 开始加载…")
            val state: LoadState<String> = loadStateCatching {
                kotlinx.coroutines.delay(800)
                "数据加载成功！"
            }
            when (state) {
                is LoadState.Loading -> log("[LoadState] 加载中…")
                is LoadState.Success -> log("[LoadState] 成功: ${state.data}")
                is LoadState.Error -> log("[LoadState] 错误: ${state.message}")
            }
        }
    }

    private fun runRetryLoadState() {
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
