package com.answufeng.arch.demo.wechat

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentProfileListBinding
import com.google.android.material.card.MaterialCardView

class ProfileFragment : BaseFragment<FragmentProfileListBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileListBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {}

    override fun onLazyLoad() {
        val items = listOf("服务", "收藏", "朋友圈", "卡包", "表情", "设置")
        binding.listRoot.removeAllViews()

        val header = MaterialCardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.demo_card_radius)
            strokeWidth = (resources.displayMetrics.density).toInt().coerceAtLeast(1)
            strokeColor = context.getColor(R.color.aw_demo_divider)
            cardElevation = 0f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 12.dp() }
        }
        header.addView(
            TextView(requireContext()).apply {
                text = "Demo User\n轻量资料卡片"
                setPadding(16.dp(), 20.dp(), 16.dp(), 20.dp())
                setTextColor(context.getColor(R.color.aw_demo_on_surface))
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
            },
        )
        binding.listRoot.addView(header)

        items.forEach { title ->
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
            }
            card.addView(
                TextView(requireContext()).apply {
                    this.text = title
                    setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                    setTextColor(context.getColor(R.color.aw_demo_on_surface))
                    textSize = 16f
                },
            )
            binding.listRoot.addView(card)
        }
    }

    private fun Int.dp(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics,
    ).toInt()
}
