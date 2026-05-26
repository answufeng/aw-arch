package com.answufeng.arch.demo.hub

import android.content.Intent
import android.os.Bundle
import com.answufeng.arch.demo.BaseMenuActivity
import com.answufeng.arch.demo.HiltDemoActivity
import com.answufeng.arch.demo.HiltMviDemoActivity
import com.answufeng.arch.demo.HiltMvpDemoActivity
import com.answufeng.arch.demo.HiltSimpleMviDemoActivity
import com.answufeng.arch.demo.R

class HiltMenuActivity : BaseMenuActivity() {
    override val pageTitle: String get() = getString(R.string.demo_hub_hilt_title)
    override val pageDesc: String get() = getString(R.string.demo_hub_hilt_desc)

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("Hilt + MVVM", intent = Intent(this, HiltDemoActivity::class.java))
        addMenuItem("Hilt + MVI", outlined = true, intent = Intent(this, HiltMviDemoActivity::class.java))
        addMenuItem(
            "Hilt + SimpleMVI",
            outlined = true,
            intent = Intent(this, HiltSimpleMviDemoActivity::class.java),
        )
        addMenuItem("Hilt + MVP", outlined = true, intent = Intent(this, HiltMvpDemoActivity::class.java))
    }
}
