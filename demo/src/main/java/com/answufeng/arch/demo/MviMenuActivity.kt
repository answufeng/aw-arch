package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle

class MviMenuActivity : BaseMenuActivity() {
    override val pageTitle: String = "MVI"
    override val pageDesc: String = "每个 Intent 单独页面：Increment / Decrement / Reset / LoadData（普通 + Hilt）。"

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("Increment（普通）", intent = Intent(this, MviIncrementDemoActivity::class.java))
        addMenuItem("Decrement（普通）", outlined = true, intent = Intent(this, MviDecrementDemoActivity::class.java))
        addMenuItem("Reset（普通）", outlined = true, intent = Intent(this, MviResetDemoActivity::class.java))
        addMenuItem("LoadData（普通）", outlined = true, intent = Intent(this, MviLoadDataDemoActivity::class.java))

        addMenuItem("Increment（Hilt）", intent = Intent(this, HiltMviIncrementDemoActivity::class.java))
        addMenuItem("Decrement（Hilt）", outlined = true, intent = Intent(this, HiltMviDecrementDemoActivity::class.java))
        addMenuItem("Reset（Hilt）", outlined = true, intent = Intent(this, HiltMviResetDemoActivity::class.java))
        addMenuItem("LoadData（Hilt）", outlined = true, intent = Intent(this, HiltMviLoadDataDemoActivity::class.java))
    }
}

