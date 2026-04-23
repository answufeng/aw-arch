package com.answufeng.arch.demo.wechat

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.FragmentWechatBinding
import com.answufeng.arch.mvi.MviFragment
import com.google.android.material.card.MaterialCardView

class WeChatFragment : MviFragment<FragmentWechatBinding, WeChatState, WeChatEvent, WeChatIntent, WeChatViewModel>() {

    override val shareViewModelWithActivity = true

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentWechatBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {}

    override fun onLazyLoad() {
        dispatch(WeChatIntent.LoadMessages)
    }

    override fun render(state: WeChatState) {
        binding.listRoot.removeAllViews()
        binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        if (state.isLoading) return

        state.messages.forEach { msg ->
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
                setOnClickListener { openChatDetail(msg) }
            }
            val inner = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            }
            inner.addView(
                TextView(requireContext()).apply {
                    text = msg.sender
                    setTextColor(context.getColor(R.color.aw_demo_on_surface))
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                },
            )
            inner.addView(
                TextView(requireContext()).apply {
                    text = msg.content
                    setTextColor(context.getColor(R.color.aw_demo_on_surface_muted))
                    textSize = 14f
                },
            )
            inner.addView(
                TextView(requireContext()).apply {
                    text = msg.time
                    setTextColor(context.getColor(R.color.aw_demo_on_surface_muted))
                    textSize = 12f
                },
            )
            card.addView(inner)
            binding.listRoot.addView(card)
        }
    }

    override fun handleEvent(event: WeChatEvent) {
        when (event) {
            is WeChatEvent.ShowMessage -> {
                android.widget.Toast.makeText(requireContext(), event.message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openChatDetail(msg: Message) {
        val args = Bundle().apply {
            putString("chat_id", msg.id)
            putString("title", msg.sender)
            putString("snippet", msg.content)
        }
        val page = ChatDetailFragment().apply { arguments = args }
        (requireActivity() as WeChatActivity).pushOverlayPage(page, "chat_detail")
    }

    private fun Int.dp(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics,
    ).toInt()
}
