package com.answufeng.arch.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import kotlinx.coroutines.launch

class FlowEventBusDemoActivity : BaseLogDemoActivity() {
    override val pageTitle: String = "FlowEventBus"
    override val pageDesc: String = "post / tryPost / postSticky / removeSticky 四种操作，本页同时订阅监听"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("[收到] ${event.message}")
        }
    }

    override fun buildActions(savedInstanceState: Bundle?) {
        addActionButton("post（挂起）") { doPost() }
        addActionButton("tryPost（非挂起）") { doTryPost() }
        addActionButton("postSticky") { doPostSticky() }
        addActionButton("removeSticky<DemoEvent>", outlined = true) { doRemove() }
        addActionButton("清空日志", outlined = true) { clearLog() }
    }

    private fun doPost() {
        lifecycleScope.launch {
            val event = DemoEvent("Hello from post() @ ${System.currentTimeMillis()}")
            FlowEventBus.post(event)
            log("[发送] ${event.message}")
        }
    }

    private fun doTryPost() {
        val event = DemoEvent("Hello from tryPost() @ ${System.currentTimeMillis()}")
        val ok = FlowEventBus.tryPost(event)
        log("[发送] ${event.message}, ok=$ok")
    }

    private fun doPostSticky() {
        lifecycleScope.launch {
            val event = DemoEvent("Sticky event @ ${System.currentTimeMillis()}")
            FlowEventBus.postSticky(event)
            log("[发送] ${event.message}")
        }
    }

    private fun doRemove() {
        FlowEventBus.removeSticky<DemoEvent>()
        log("[执行] removeSticky<DemoEvent>")
    }
}
