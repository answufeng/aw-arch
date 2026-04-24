package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import kotlinx.coroutines.launch

class BusPostDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "FlowEventBus · post"
    override val pageDesc: String = "演示挂起式 post：发送后在本页监听端收到事件。"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[收到] ${event.message}")
        }
    }

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("post（挂起）") { doPost() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun doPost() {
        lifecycleScope.launch {
            val event = DemoEvent("Hello from post() @ ${System.currentTimeMillis()}")
            FlowEventBus.post(event)
            log("[发送] ${event.message}")
        }
    }
}

