package com.answufeng.arch.demo

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentNavHomeBinding
import com.answufeng.arch.demo.databinding.FragmentNavDetailBinding
import com.answufeng.arch.demo.databinding.FragmentNavSettingsBinding
import com.answufeng.arch.nav.AwNav
import com.answufeng.arch.nav.AwNavRoute
import com.answufeng.arch.nav.NavAnim
import com.answufeng.arch.nav.OnGoBackListener
import com.google.android.material.card.MaterialCardView

@AwNavRoute("home")
class HomeFragment : BaseFragment<FragmentNavHomeBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNavHomeBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val nav = AwNav.from(this)
        binding.tvStackInfo.text = getString(R.string.demo_nav_stack_depth, nav.stackDepth)

        val items = listOf(
            "产品详情" to "左右滑动（默认动画）",
            "用户中心" to "上下滑动动画",
            "订单追踪" to "淡入淡出动画",
        )
        binding.listRoot.removeAllViews()
        items.forEachIndexed { index, (title, subtitle) ->
            binding.listRoot.addView(buildNavCard(title, subtitle) {
                when (index) {
                    0 -> nav.navigate(
                        "detail",
                        bundleOf("key" to "产品A", "index" to index),
                    ) // 默认 SLIDE_HORIZONTAL

                    1 -> nav.navigate("settings") { anim = NavAnim.SLIDE_VERTICAL }

                    2 -> nav.navigate(
                        "detail",
                        bundleOf("key" to "订单#1024", "index" to index),
                    ) { anim = NavAnim.FADE }
                }
            })
        }
    }

    private fun buildNavCard(title: String, subtitle: String, onClick: () -> Unit): MaterialCardView {
        return MaterialCardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.demo_card_radius)
            strokeWidth = (resources.displayMetrics.density).toInt().coerceAtLeast(1)
            strokeColor = context.getColor(R.color.aw_demo_divider)
            cardElevation = 0f
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 8.dp() }
            setOnClickListener { onClick() }
            val inner = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            }
            inner.addView(
                TextView(context).apply {
                    text = title
                    setTextColor(context.getColor(R.color.aw_demo_on_surface))
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                },
            )
            inner.addView(
                TextView(context).apply {
                    text = subtitle
                    setTextColor(context.getColor(R.color.aw_demo_on_surface_muted))
                    textSize = 13f
                },
            )
            addView(inner)
        }
    }

    private fun Int.dp(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics,
    ).toInt()
}

@AwNavRoute("detail")
class DetailFragment : BaseFragment<FragmentNavDetailBinding>(), OnGoBackListener {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNavDetailBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val nav = AwNav.from(this)
        val key = arguments?.getString("key") ?: getString(R.string.demo_nav_no_args)
        val index = arguments?.getInt("index") ?: 0

        binding.toolbar.title = key
        binding.toolbar.setNavigationOnClickListener {
            if (!nav.back()) {
                // 根页面，无法返回
            }
        }

        binding.tvBreadcrumb.text = getString(R.string.demo_nav_detail_breadcrumb, nav.stackDepth)
        binding.tvArgs.text = getString(R.string.demo_nav_detail_args, key, index)
        binding.tvStackInfo.text = getString(R.string.demo_nav_stack_depth, nav.stackDepth)

        binding.btnGoDeeper.setOnClickListener {
            val nextIndex = index + 1
            nav.navigate(
                "detail",
                bundleOf("key" to "第${nextIndex + 1}层详情", "index" to nextIndex),
            ) // 默认 SLIDE_HORIZONTAL
        }

        binding.btnBackToHome.setOnClickListener {
            nav.backTo("home")
        }

        binding.btnClearAndNavigate.setOnClickListener {
            nav.clearAndNavigate("settings") { anim = NavAnim.FADE }
        }
    }

    override fun onGoBack(): Boolean {
        if (binding.scrollView.scrollY > 0) {
            binding.scrollView.smoothScrollTo(0, 0)
            return false // 拦截返回，先滚动到顶部
        }
        return true // 允许返回
    }
}

@AwNavRoute("settings")
class SettingsFragment : BaseFragment<FragmentNavSettingsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNavSettingsBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val nav = AwNav.from(this)
        binding.toolbar.setNavigationOnClickListener {
            nav.back()
        }
        binding.tvStackInfo.text = getString(R.string.demo_nav_stack_depth, nav.stackDepth)

        val items = listOf("通知设置", "隐私与安全", "外观主题", "关于应用")
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
            card.addView(
                TextView(requireContext()).apply {
                    text = title
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
