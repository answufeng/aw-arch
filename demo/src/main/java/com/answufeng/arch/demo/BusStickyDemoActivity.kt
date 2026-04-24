package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import kotlinx.coroutines.launch

class BusStickyDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "FlowEventBus · postSticky"
    override val pageDesc: String = "演示 postSticky：新订阅者进入页面后也能立即收到最近一次 Sticky。"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[收到] ${event.message}")
        }
    }

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("postSticky（挂起）") { doPostSticky() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun doPostSticky() {
        lifecycleScope.launch {
            val event = DemoEvent("Sticky event @ ${System.currentTimeMillis()}")
            FlowEventBus.postSticky(event)
            log("[发送] ${event.message}")
        }
    }
}

