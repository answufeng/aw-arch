package com.answufeng.arch.demo

import android.app.Application
import android.content.pm.ApplicationInfo
import com.answufeng.arch.config.AwArch
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AwArch.init {
            strictMainThreadForAwNav = debuggable
            logAwNavThrottledNavigations = debuggable
        }
    }
}
