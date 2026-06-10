package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.answufeng.arch.demo.R
import com.answufeng.arch.nav.AwNavHostFragment
import com.answufeng.arch.nav.NavAnim
import kotlinx.coroutines.launch

/**
 * 盖在 Tab + BottomNav 之上的全屏层；内层页面由 [AwNav]（child 容器）管理。
 *
 * 不同路由使用不同动画：
 * - chat_detail / contact_detail：左右滑动（默认页面跳转）
 * - chat_info：从底部滑入（查看详情）
 * - contact_extra：淡入淡出（轻量扩展信息）
 */
class WeChatOverlayHostFragment : AwNavHostFragment(R.layout.fragment_overlay_host, R.id.inner_container) {

    /** 返回键由 WeChatActivity 的 BackDispatcherChain 统一管理，不使用 AwNav 内置处理。 */
    override val handleBackPressed: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nav.register {
            route<ChatDetailFragment>("chat_detail")
            route<ChatInfoFragment>("chat_info")
            route<ContactDetailFragment>("contact_detail")
            route<ContactExtraFragment>("contact_extra")
        }
        // 路由变化时通知 Activity 更新返回链状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                nav.currentRouteFlow.collect {
                    (activity as? WeChatActivity)?.updateBackChainState()
                }
            }
        }
    }

    fun navigate(route: String, args: android.os.Bundle? = null) {
        val anim = when (route) {
            "chat_info" -> NavAnim.SLIDE_VERTICAL    // 从底部滑入：查看聊天详情
            "contact_extra" -> NavAnim.FADE           // 淡入淡出：轻量扩展信息
            else -> NavAnim.SLIDE_HORIZONTAL          // 左右滑动：默认页面跳转
        }
        nav.navigate(route, args) { this.anim = anim }
    }

    fun popInner(): Boolean = nav.back()

    val innerStackDepth: Int
        get() = nav.stackDepth
}
