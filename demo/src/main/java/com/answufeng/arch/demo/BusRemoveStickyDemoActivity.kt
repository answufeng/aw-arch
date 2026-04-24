package com.answufeng.arch.demo

import android.os.Bundle
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle

class BusRemoveStickyDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "FlowEventBus · removeSticky"
    override val pageDesc: String = "演示 removeSticky：移除指定类型 Sticky，后续新订阅者不会自动收到。"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[收到] ${event.message}")
        }
    }

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("removeSticky<DemoEvent>") { doRemove() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun doRemove() {
        FlowEventBus.removeSticky<DemoEvent>()
        log("[执行] removeSticky<DemoEvent>")
    }
}

