package com.answufeng.arch.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topBar))

        findViewById<Button>(R.id.btnLoadState).setOnClickListener { open(LoadStateDemoActivity::class.java) }
        findViewById<Button>(R.id.btnRetryLoadState).setOnClickListener { open(LoadStateDemoActivity::class.java) }

        findViewById<Button>(R.id.btnPostEvent).setOnClickListener { open(FlowEventBusDemoActivity::class.java) }
        findViewById<Button>(R.id.btnTryPostEvent).setOnClickListener { open(FlowEventBusDemoActivity::class.java) }
        findViewById<Button>(R.id.btnStickyEvent).setOnClickListener { open(FlowEventBusDemoActivity::class.java) }
        findViewById<Button>(R.id.btnRemoveSticky).setOnClickListener { open(FlowEventBusDemoActivity::class.java) }

        findViewById<Button>(R.id.btnMvvm).setOnClickListener { open(MvvmDemoActivity::class.java) }
        findViewById<Button>(R.id.btnHiltMvvm).setOnClickListener { open(HiltDemoActivity::class.java) }

        findViewById<Button>(R.id.btnMvi).setOnClickListener { open(MviDemoActivity::class.java) }
        findViewById<Button>(R.id.btnSimpleMvi).setOnClickListener { open(SimpleMviDemoActivity::class.java) }
        findViewById<Button>(R.id.btnHiltMvi).setOnClickListener { open(HiltMviDemoActivity::class.java) }

        findViewById<Button>(R.id.btnMvp).setOnClickListener { open(MvpDemoActivity::class.java) }
        findViewById<Button>(R.id.btnHiltMvp).setOnClickListener { open(HiltMvpDemoActivity::class.java) }

        findViewById<Button>(R.id.btnNav).setOnClickListener { open(AwNavBasicRouteDemoActivity::class.java) }
        findViewById<Button>(R.id.btnNavInterceptor).setOnClickListener { open(AwNavInterceptorDemoActivity::class.java) }
        findViewById<Button>(R.id.btnWeChat).setOnClickListener {
            open(com.answufeng.arch.demo.wechat.WeChatMenuActivity::class.java)
        }
    }

    private fun open(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }
}
