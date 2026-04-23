package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentChatDetailBinding
class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title") ?: "会话"
        val snippet = arguments?.getString("snippet") ?: ""
        binding.tvBreadcrumb.text = getString(R.string.wechat_layer_chat_detail)
        binding.tvTitle.text = title
        binding.tvSnippet.text = snippet
        binding.tvThread.text = buildString {
            repeat(8) {
                append("[$title] 模拟消息 ${it + 1}：用于演示长列表滚动。\n\n")
            }
        }
        binding.btnOpenInfo.setOnClickListener {
            val args = bundleOf(
                "chat_id" to arguments?.getString("chat_id"),
                "title" to title,
            )
            val page = ChatInfoFragment().apply { arguments = args }
            (requireActivity() as WeChatActivity).pushOverlayPage(page, "chat_info")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
