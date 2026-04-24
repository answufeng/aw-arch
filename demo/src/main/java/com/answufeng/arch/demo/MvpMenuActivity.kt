package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle

class MvpMenuActivity : BaseMenuActivity() {
    override val pageTitle: String = "MVP"
    override val pageDesc: String = "每个功能点单独页面（普通 + Hilt）。"

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("Increment（普通）", intent = Intent(this, MvpIncrementDemoActivity::class.java))
        addMenuItem("Decrement（普通）", outlined = true, intent = Intent(this, MvpDecrementDemoActivity::class.java))
        addMenuItem("Reset（普通）", outlined = true, intent = Intent(this, MvpResetDemoActivity::class.java))
        addMenuItem("LoadData（普通）", outlined = true, intent = Intent(this, MvpLoadDataDemoActivity::class.java))

        addMenuItem("Increment（Hilt）", intent = Intent(this, HiltMvpIncrementDemoActivity::class.java))
        addMenuItem("Decrement（Hilt）", outlined = true, intent = Intent(this, HiltMvpDecrementDemoActivity::class.java))
        addMenuItem("Reset（Hilt）", outlined = true, intent = Intent(this, HiltMvpResetDemoActivity::class.java))
        addMenuItem("LoadData（Hilt）", outlined = true, intent = Intent(this, HiltMvpLoadDataDemoActivity::class.java))
    }
}

