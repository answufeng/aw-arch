package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentWechatBinding

class ProfileFragment : BaseFragment<FragmentWechatBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWechatBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
        val items = listOf("服务", "收藏", "朋友圈", "卡包", "表情", "设置")
        binding.container.removeAllViews()
        val header = TextView(requireContext()).apply {
            text = "用户名：Demo User"
            setPadding(0, 32, 0, 16)
            textSize = 20f
        }
        binding.container.addView(header)
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
