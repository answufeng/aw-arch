package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentChatInfoBinding

class ChatInfoFragment : Fragment() {

    private var _binding: FragmentChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvBreadcrumb.text = getString(R.string.wechat_layer_chat_info)
        binding.tvChatName.text = arguments?.getString("title") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
