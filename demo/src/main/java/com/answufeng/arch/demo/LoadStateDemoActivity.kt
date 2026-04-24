package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadStateDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "LoadState"
    override val pageDesc: String = "演示 loadStateCatching：在同一个调用里得到 Loading/Success/Error 三态结果。"

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("开始加载") { runDemo() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun runDemo() {
        lifecycleScope.launch {
            log("[LoadState] 开始加载…")
            val state: LoadState<String> = loadStateCatching {
                delay(800)
                "数据加载成功！"
            }
            when (state) {
                is LoadState.Loading -> log("[LoadState] 加载中…")
                is LoadState.Success -> log("[LoadState] 成功: ${state.data}")
                is LoadState.Error -> log("[LoadState] 错误: ${state.message}")
            }
        }
    }
}

