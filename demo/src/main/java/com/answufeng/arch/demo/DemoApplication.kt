package com.answufeng.arch.demo

import android.app.Application
import com.answufeng.arch.config.AwArch
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AwArch.init { }
    }
}
