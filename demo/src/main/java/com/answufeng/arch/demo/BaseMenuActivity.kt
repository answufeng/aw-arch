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

    protected fun addSectionTitle(title: String) {
        val tv =
            TextView(this).apply {
                text = title
                setTextAppearance(R.style.TextAppearance_AwArchDemo_SectionLabel)
                val top = if (actions.childCount > 0) R.dimen.demo_space_md else R.dimen.demo_space_sm
                setPadding(0, resources.getDimensionPixelSize(top), 0, resources.getDimensionPixelSize(R.dimen.demo_space_sm))
            }
        actions.addView(
            tv,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
    }

    protected fun addMenuItem(
        text: String,
        subtitle: String = "",
        outlined: Boolean = false,
        intent: Intent,
    ) {
        val container =
            layoutInflater.inflate(R.layout.item_demo_menu_action, actions, false)
        container.findViewById<TextView>(R.id.title).text = text
        val sub = container.findViewById<TextView>(R.id.subtitle)
        if (subtitle.isBlank()) {
            sub.visibility = View.GONE
        } else {
            sub.text = subtitle
        }

        container.findViewById<MaterialButton>(R.id.btnOpen).setOnClickListener { startActivity(intent) }

        val lp =
            ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        if (actions.childCount > 0) {
            lp.topMargin = resources.getDimensionPixelSize(R.dimen.demo_space_sm)
        }
        actions.addView(container, lp)
    }

    protected abstract fun buildMenu(savedInstanceState: Bundle?)
}
