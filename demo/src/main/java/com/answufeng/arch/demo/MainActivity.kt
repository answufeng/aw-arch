package com.answufeng.arch.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.arch.event.FlowEventBus
import com.answufeng.arch.ext.collectOnLifecycle
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import com.answufeng.arch.state.retryLoadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

data class DemoEvent(val message: String)

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = TextView(this).apply { textSize = 14f }
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(tvLog)

        container.addView(button("Test LoadState") {
            lifecycleScope.launch {
                val state: LoadState<String> = loadStateCatching {
                    delay(500)
                    "Data loaded successfully!"
                }
                state.onLoading { log("Loading...") }
                state.onSuccess { log("Success: $it") }
                state.onError { log("Error: ${it.message}") }
            }
        })

        container.addView(button("Test RetryLoadState") {
            lifecycleScope.launch {
                var attempt = 0
                val state = retryLoadState(times = 3, initialDelayMillis = 500) {
                    attempt++
                    if (attempt < 3) throw RuntimeException("Attempt $attempt failed")
                    "Recovered on attempt $attempt"
                }
                state.onSuccess { log("Success: $it") }
                state.onError { log("Failed after retries: ${it.message}") }
            }
        })

        container.addView(button("Post Event") {
            lifecycleScope.launch {
                FlowEventBus.post(DemoEvent("Hello from FlowEventBus!"))
                log("Event posted")
            }
        })

        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("Received event: ${event.message}")
        }
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply { this.text = text; setOnClickListener { onClick() } }
    }

    private fun log(msg: String) { tvLog.append("$msg\n") }
}
