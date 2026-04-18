package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentWechatBinding

class ContactFragment : BaseFragment<FragmentWechatBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWechatBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
        val contacts = listOf("张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十")
        binding.container.removeAllViews()
        contacts.forEach { name ->
            val tv = TextView(requireContext()).apply {
                text = name
                setPadding(0, 24, 0, 24)
                textSize = 16f
            }
            binding.container.addView(tv)
        }
    }
}
