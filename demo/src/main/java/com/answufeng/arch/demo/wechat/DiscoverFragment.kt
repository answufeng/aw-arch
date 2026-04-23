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
import com.answufeng.arch.demo.databinding.FragmentDiscoverListBinding
import com.google.android.material.card.MaterialCardView

class DiscoverFragment : BaseFragment<FragmentDiscoverListBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDiscoverListBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {}

    override fun onLazyLoad() {
        val items = listOf("朋友圈", "视频号", "直播", "扫一扫", "摇一摇", "看一看", "搜一搜", "附近")
        binding.listRoot.removeAllViews()
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
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            }
            row.addView(
                TextView(requireContext()).apply {
                    text = title
                    setTextColor(context.getColor(R.color.aw_demo_on_surface))
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                },
            )
            card.addView(row)
            binding.listRoot.addView(card)
        }
    }

    private fun Int.dp(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics,
    ).toInt()
}
