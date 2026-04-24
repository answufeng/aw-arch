package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle

class MvvmMenuActivity : BaseMenuActivity() {
    override val pageTitle: String = "MVVM"
    override val pageDesc: String = "每个功能点单独页面：Loading / Toast / Error / NavigateBack。"

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("Loading（普通）", intent = Intent(this, MvvmLoadingDemoActivity::class.java))
        addMenuItem("Toast（普通）", outlined = true, intent = Intent(this, MvvmToastDemoActivity::class.java))
        addMenuItem("Error（普通）", outlined = true, intent = Intent(this, MvvmErrorDemoActivity::class.java))
        addMenuItem("NavigateBack（普通）", outlined = true, intent = Intent(this, MvvmBackDemoActivity::class.java))

        addMenuItem("MVVM（Hilt）", intent = Intent(this, HiltDemoActivity::class.java))
    }
}

