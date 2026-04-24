package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

abstract class BaseMenuActivity : AppCompatActivity() {

    abstract val pageTitle: String
    open val pageDesc: String = ""

    private lateinit var actions: ViewGroup
    private lateinit var tvDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_menu)

        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        topBar.title = pageTitle
        topBar.setNavigationOnClickListener { finish() }

        tvDesc = findViewById(R.id.tvDesc)
        actions = findViewById(R.id.actions)

        tvDesc.text = pageDesc
        if (pageDesc.isBlank()) tvDesc.visibility = View.GONE

        buildMenu(savedInstanceState)
    }

    protected fun addMenuItem(
        text: String,
        outlined: Boolean = false,
        intent: Intent,
    ) {
        val defStyleAttr = if (outlined) {
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        } else {
            com.google.android.material.R.attr.materialButtonStyle
        }
        val btn = MaterialButton(this, null, defStyleAttr).apply { this.text = text }
        btn.setOnClickListener { startActivity(intent) }

        val lp = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (actions.childCount > 0) {
            lp.topMargin = resources.getDimensionPixelSize(R.dimen.demo_space_sm)
        }
        actions.addView(btn, lp)
    }

    protected abstract fun buildMenu(savedInstanceState: Bundle?)
}

