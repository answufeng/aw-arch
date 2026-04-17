package com.answufeng.arch.demo.wechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.answufeng.arch.base.BaseFragment
import com.answufeng.arch.demo.databinding.FragmentProfileBinding

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun onLazyLoad() {
    }
}
