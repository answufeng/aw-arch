package com.answufeng.arch.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        tvLog = findViewById(R.id.tvLog)
        val container = findViewById<LinearLayout>(R.id.container)

        container.addView(button("Test LoadState") {
            lifecycleScope.launch {
                val state: LoadState<String> = loadStateCatching {
                    delay(500)
                    "Data loaded successfully!"
                }
                when (state) {
                    is LoadState.Loading -> log("Loading...")
                    is LoadState.Success -> log("Success: ${state.data}")
                    is LoadState.Error -> log("Error: ${state.message}")
                }
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
                when (state) {
                    is LoadState.Success -> log("Success: ${state.data}")
                    is LoadState.Error -> log("Failed after retries: ${state.message}")
                    is LoadState.Loading -> log("Loading...")
                }
            }
        })

        container.addView(button("Post Event (suspend)") {
            lifecycleScope.launch {
                FlowEventBus.post(DemoEvent("Hello from FlowEventBus!"))
                log("Event posted (suspend)")
            }
        })

        container.addView(button("Post Event (tryPost)") {
            val success = FlowEventBus.tryPost(DemoEvent("Hello from tryPost!"))
            log("tryPost result: $success")
        })

        container.addView(button("Post Sticky Event") {
            lifecycleScope.launch {
                FlowEventBus.postSticky(DemoEvent("Sticky event!"))
                log("Sticky event posted")
            }
        })

        container.addView(button("Open MVVM Demo") {
            startActivity(android.content.Intent(this, MvvmDemoActivity::class.java))
        })

        container.addView(button("Open MVI Demo") {
            startActivity(android.content.Intent(this, MviDemoActivity::class.java))
        })

        container.addView(button("Open Nav Demo") {
            startActivity(android.content.Intent(this, NavDemoActivity::class.java))
        })

        FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { event ->
            log("Received event: ${event.message}")
        }
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply { this.text = text; setOnClickListener { onClick() } }
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
        android.util.Log.d("Demo", msg)
    }
}
