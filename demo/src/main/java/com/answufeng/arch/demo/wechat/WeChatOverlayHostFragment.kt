package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.answufeng.arch.R as ArchR
import com.answufeng.arch.demo.R

/**
 * 盖在「Tab 内容 + BottomNav」之上的全屏层；**内层**跳转走 [childFragmentManager]，
 * 与 [AwNav] 的返回栈隔离，避免共用 Activity 返回栈打乱路由状态。
 */
class WeChatOverlayHostFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_overlay_host, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.addOnBackStackChangedListener {
            (activity as? WeChatActivity)?.syncWeChatChrome()
        }
    }

    fun pushPage(fragment: Fragment, tag: String) {
        childFragmentManager.beginTransaction()
            .setCustomAnimations(
                ArchR.anim.aw_nav_slide_in_right,
                ArchR.anim.aw_nav_slide_out_left,
                ArchR.anim.aw_nav_slide_in_left,
                ArchR.anim.aw_nav_slide_out_right,
            )
            .replace(R.id.inner_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    val innerStackDepth: Int
        get() = childFragmentManager.backStackEntryCount
}
