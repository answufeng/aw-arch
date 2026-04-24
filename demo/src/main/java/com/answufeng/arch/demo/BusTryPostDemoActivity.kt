package com.answufeng.arch.demo

import android.os.Bundle
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle

class BusTryPostDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "FlowEventBus · tryPost"
    override val pageDesc: String = "演示非挂起 tryPost：立即返回是否投递成功。"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[收到] ${event.message}")
        }
    }

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("tryPost") { doTryPost() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun doTryPost() {
        val event = DemoEvent("Hello from tryPost() @ ${System.currentTimeMillis()}")
        val ok = FlowEventBus.tryPost(event)
        log("[发送] ${event.message}, ok=$ok")
    }
}

