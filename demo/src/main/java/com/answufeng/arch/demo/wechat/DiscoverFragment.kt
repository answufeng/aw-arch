package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentWechatBinding

class DiscoverFragment : BaseFragment<FragmentWechatBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWechatBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
        val items = listOf("朋友圈", "视频号", "直播", "扫一扫", "摇一摇", "看一看", "搜一搜", "附近")
        binding.container.removeAllViews()
        items.forEach { item ->
            val tv = TextView(requireContext()).apply {
                text = item
                setPadding(0, 24, 0, 24)
                textSize = 16f
            }
            binding.container.addView(tv)
        }
    }
}
