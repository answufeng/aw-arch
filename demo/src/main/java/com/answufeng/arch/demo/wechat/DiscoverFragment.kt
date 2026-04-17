package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentDiscoverBinding

class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDiscoverBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
    }
}
