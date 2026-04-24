package com.answufeng.arch.demo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

abstract class BaseLogDemoActivity : AppCompatActivity() {

    protected lateinit var tvLog: TextView
        private set

    protected lateinit var tvDesc: TextView
        private set

    private lateinit var actions: ViewGroup

    abstract val pageTitle: String
    open val pageDesc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_log)

        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        topBar.title = pageTitle
        topBar.setNavigationOnClickListener { finish() }

        tvLog = findViewById(R.id.tvLog)
        tvDesc = findViewById(R.id.tvDesc)
        actions = findViewById(R.id.actions)

        tvDesc.text = pageDesc
        if (pageDesc.isBlank()) {
            tvDesc.visibility = View.GONE
        }

        buildActions(savedInstanceState)
    }

    protected fun addActionButton(
        text: String,
        outlined: Boolean = false,
        onClick: () -> Unit,
    ) {
        val defStyleAttr = if (outlined) {
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        } else {
            com.google.android.material.R.attr.materialButtonStyle
        }
        val btn = MaterialButton(this, null, defStyleAttr).apply { this.text = text }
        btn.setOnClickListener { onClick() }

        val lp = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (actions.childCount > 0) {
            lp.topMargin = resources.getDimensionPixelSize(R.dimen.demo_space_sm)
        }
        actions.addView(btn, lp)
    }

    protected fun log(msg: String) {
        tvLog.append("$msg\n")
    }

    protected fun clearLog() {
        tvLog.text = ""
    }

    protected abstract fun buildActions(savedInstanceState: Bundle?)
}

