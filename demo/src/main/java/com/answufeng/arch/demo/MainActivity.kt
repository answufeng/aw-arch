package com.answufeng.arch.demo

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topBar))

        findViewById<Button>(R.id.btnLoadState).setOnClickListener { openLoadStateDemo() }
        findViewById<Button>(R.id.btnRetryLoadState).setOnClickListener { openRetryLoadStateDemo() }
        findViewById<Button>(R.id.btnPostEvent).setOnClickListener { openBusPostDemo() }
        findViewById<Button>(R.id.btnTryPostEvent).setOnClickListener { openBusTryPostDemo() }
        findViewById<Button>(R.id.btnStickyEvent).setOnClickListener { openBusStickyDemo() }
        findViewById<Button>(R.id.btnRemoveSticky).setOnClickListener { openBusRemoveStickyDemo() }

        findViewById<Button>(R.id.btnMvvm).setOnClickListener { openMvvmDemo() }
        findViewById<Button>(R.id.btnMvi).setOnClickListener { openMviDemo() }
        findViewById<Button>(R.id.btnMvp).setOnClickListener { openMvpDemo() }
        findViewById<Button>(R.id.btnNav).setOnClickListener { openNavDemo() }
        findViewById<Button>(R.id.btnWeChat).setOnClickListener { openWeChatDemo() }

        findViewById<View>(R.id.btnPlaybook).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.demo_playbook_title)
                .setMessage(R.string.demo_playbook_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun openLoadStateDemo() {
        startActivity(android.content.Intent(this, LoadStateDemoActivity::class.java))
    }

    private fun openRetryLoadStateDemo() {
        startActivity(android.content.Intent(this, RetryLoadStateDemoActivity::class.java))
    }

    private fun openBusPostDemo() {
        startActivity(android.content.Intent(this, BusPostDemoActivity::class.java))
    }

    private fun openBusTryPostDemo() {
        startActivity(android.content.Intent(this, BusTryPostDemoActivity::class.java))
    }

    private fun openBusStickyDemo() {
        startActivity(android.content.Intent(this, BusStickyDemoActivity::class.java))
    }

    private fun openBusRemoveStickyDemo() {
        startActivity(android.content.Intent(this, BusRemoveStickyDemoActivity::class.java))
    }

    private fun openMvvmDemo() {
        startActivity(android.content.Intent(this, MvvmMenuActivity::class.java))
    }

    private fun openMviDemo() {
        startActivity(android.content.Intent(this, MviMenuActivity::class.java))
    }

    private fun openMvpDemo() {
        startActivity(android.content.Intent(this, MvpMenuActivity::class.java))
    }

    private fun openNavDemo() {
        startActivity(android.content.Intent(this, AwNavMenuActivity::class.java))
    }

    private fun openWeChatDemo() {
        startActivity(android.content.Intent(this, com.answufeng.arch.demo.wechat.WeChatMenuActivity::class.java))
    }
}
