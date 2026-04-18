package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.answufeng.arch.mvi.MviFragment
import com.answufeng.arch.demo.databinding.FragmentWechatBinding

class WeChatFragment : MviFragment<FragmentWechatBinding, WeChatState, WeChatEvent, WeChatIntent, WeChatViewModel>() {

    override val shareViewModelWithActivity = true

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWechatBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
        dispatch(WeChatIntent.LoadMessages)
    }

    override fun render(state: WeChatState) {
        binding.container.removeAllViews()
        if (state.isLoading) {
            val tv = TextView(requireContext()).apply { text = "加载中..." }
            binding.container.addView(tv)
        } else {
            state.messages.forEach { msg ->
                val tv = TextView(requireContext()).apply {
                    text = "${msg.sender}: ${msg.content}"
                    setPadding(0, 16, 0, 16)
                    textSize = 16f
                }
                binding.container.addView(tv)
            }
        }
    }

    override fun handleEvent(event: WeChatEvent) {
        when (event) {
            is WeChatEvent.ShowMessage -> {
                android.widget.Toast.makeText(requireContext(), event.message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
