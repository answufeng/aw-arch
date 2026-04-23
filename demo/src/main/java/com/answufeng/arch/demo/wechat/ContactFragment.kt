package com.answufeng.arch.demo.wechat

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentContactListBinding
import com.google.android.material.card.MaterialCardView

class ContactFragment : BaseFragment<FragmentContactListBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentContactListBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {}

    override fun onLazyLoad() {
        val contacts = listOf("张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十")
        binding.listRoot.removeAllViews()
        contacts.forEach { name ->
            binding.listRoot.addView(buildContactRow(name))
        }
    }

    private fun buildContactRow(name: String): MaterialCardView {
        val card = MaterialCardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.demo_card_radius)
            strokeWidth = (resources.displayMetrics.density).toInt().coerceAtLeast(1)
            strokeColor = context.getColor(R.color.aw_demo_divider)
            cardElevation = 0f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 8.dp() }
            setOnClickListener {
                val page = ContactDetailFragment().apply { arguments = bundleOf("name" to name) }
                (requireActivity() as WeChatActivity).pushOverlayPage(page, "contact_detail")
            }
        }
        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
        }
        inner.addView(
            TextView(requireContext()).apply {
                text = name
                setTextColor(context.getColor(R.color.aw_demo_on_surface))
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
            },
        )
        inner.addView(
            TextView(requireContext()).apply {
                text = "点击进第 2 层：联系人详情（可再进第 3 层）"
                setTextColor(context.getColor(R.color.aw_demo_on_surface_muted))
                textSize = 13f
            },
        )
        card.addView(inner)
        return card
    }

    private fun Int.dp(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics,
    ).toInt()
}
