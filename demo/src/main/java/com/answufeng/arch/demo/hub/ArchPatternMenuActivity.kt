package com.answufeng.arch.demo.hub

import android.content.Intent
import android.os.Bundle
import com.answufeng.arch.demo.BaseMenuActivity
import com.answufeng.arch.demo.MviDemoActivity
import com.answufeng.arch.demo.MvpDemoActivity
import com.answufeng.arch.demo.MvvmDemoActivity
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.SimpleMviDemoActivity

class ArchPatternMenuActivity : BaseMenuActivity() {
    override val pageTitle: String get() = getString(R.string.demo_hub_arch_title)
    override val pageDesc: String get() = getString(R.string.demo_hub_arch_desc)

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("MVVM", subtitle = "ViewModel + UiEvent", intent = Intent(this, MvvmDemoActivity::class.java))
        addMenuItem("MVI", subtitle = "State / Effect / Intent", intent = Intent(this, MviDemoActivity::class.java))
        addMenuItem(
            "SimpleMVI",
            subtitle = "无独立 Effect 通道",
            outlined = true,
            intent = Intent(this, SimpleMviDemoActivity::class.java),
        )
        addMenuItem("MVP", subtitle = "Contract + Presenter", outlined = true, intent = Intent(this, MvpDemoActivity::class.java))
    }
}
