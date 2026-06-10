package com.answufeng.arch.demo.userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.databinding.ActivityUserListBinding
import com.answufeng.arch.mvi.MviActivity

class UserListActivity :
    MviActivity<ActivityUserListBinding, UserListState, UserListEffect, UserListIntent, UserListViewModel>() {

    private val adapter = UserListAdapter { user ->
        dispatch(UserListIntent.UserClicked(user))
    }

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityUserListBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            dispatch(UserListIntent.Refresh)
        }

        // 加载更多：滚动到底部时触发
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 2) {
                    dispatch(UserListIntent.LoadMore)
                }
            }
        })

        // 重试
        binding.btnRetry.setOnClickListener { dispatch(UserListIntent.Retry) }

        // FAB 刷新
        binding.fabRefresh.setOnClickListener { dispatch(UserListIntent.Refresh) }

        // 初始加载
        if (savedInstanceState == null) {
            dispatch(UserListIntent.LoadUsers)
        }
    }

    override fun render(state: UserListState) {
        // 初始加载
        binding.progressInitial.visibility = if (state.isLoading && state.users.isEmpty()) View.VISIBLE else View.GONE
        binding.swipeRefresh.isRefreshing = state.isLoading && state.users.isNotEmpty()

        // 加载更多
        binding.progressMore.visibility = if (state.isLoadingMore) View.VISIBLE else View.GONE

        // 列表数据
        adapter.submitList(state.users)

        // 空状态
        binding.tvEmpty.visibility = if (!state.isLoading && state.users.isEmpty() && state.error == null) View.VISIBLE else View.GONE

        // 错误状态
        binding.layoutError.visibility = if (state.error != null && state.users.isEmpty()) View.VISIBLE else View.GONE
        if (state.error != null) {
            binding.tvError.text = state.error
        }
    }

    override fun handleEvent(event: UserListEffect) {
        when (event) {
            is UserListEffect.ShowToast ->
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            is UserListEffect.ShowError ->
                Toast.makeText(this, "错误: ${event.message}", Toast.LENGTH_LONG).show()
        }
    }
}
