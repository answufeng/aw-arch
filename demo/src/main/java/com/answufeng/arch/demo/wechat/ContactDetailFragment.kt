package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentContactDetailBinding
class ContactDetailFragment : Fragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("name") ?: ""
        binding.tvBreadcrumb.text = getString(R.string.wechat_contact_layer_2)
        binding.tvName.text = name
        binding.btnOpenExtra.setOnClickListener {
            val page = ContactExtraFragment().apply { arguments = bundleOf("name" to name) }
            (requireActivity() as WeChatActivity).pushOverlayPage(page, "contact_extra")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
