package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.arch.demo.hub.ArchPatternMenuActivity
import com.answufeng.arch.demo.hub.HiltMenuActivity
import com.answufeng.arch.demo.hub.NavMenuActivity
import com.answufeng.arch.demo.loadstate.LoadStateListActivity
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topBar))

        bindEntry(
            root = findViewById(R.id.entryArch),
            iconRes = R.drawable.ic_demo_arch_24,
            title = getString(R.string.demo_entry_arch_title),
            subtitle = getString(R.string.demo_entry_arch_subtitle),
        ) { startActivity(Intent(this, ArchPatternMenuActivity::class.java)) }

        bindEntry(
            root = findViewById(R.id.entryHilt),
            iconRes = R.drawable.ic_demo_hilt_24,
            title = getString(R.string.demo_entry_hilt_title),
            subtitle = getString(R.string.demo_entry_hilt_subtitle),
        ) { startActivity(Intent(this, HiltMenuActivity::class.java)) }

        bindEntry(
            root = findViewById(R.id.entryLoadState),
            iconRes = R.drawable.ic_demo_state_24,
            title = getString(R.string.demo_entry_load_state_title),
            subtitle = getString(R.string.demo_entry_load_state_subtitle),
        ) { startActivity(Intent(this, LoadStateListActivity::class.java)) }

        bindEntry(
            root = findViewById(R.id.entryEventBus),
            iconRes = R.drawable.ic_demo_bus_24,
            title = getString(R.string.demo_entry_event_bus_title),
            subtitle = getString(R.string.demo_entry_event_bus_subtitle),
        ) { startActivity(Intent(this, FlowEventBusDemoActivity::class.java)) }

        bindEntry(
            root = findViewById(R.id.entryNav),
            iconRes = R.drawable.ic_demo_nav_24,
            title = getString(R.string.demo_entry_nav_title),
            subtitle = getString(R.string.demo_entry_nav_subtitle),
        ) { startActivity(Intent(this, NavMenuActivity::class.java)) }
    }

    private fun bindEntry(
        root: View,
        iconRes: Int,
        title: String,
        subtitle: String,
        onClick: () -> Unit,
    ) {
        root.findViewById<ImageView>(R.id.icon).setImageResource(iconRes)
        root.findViewById<TextView>(R.id.title).text = title
        root.findViewById<TextView>(R.id.subtitle).text = subtitle
        root.setOnClickListener { onClick() }
    }
}
