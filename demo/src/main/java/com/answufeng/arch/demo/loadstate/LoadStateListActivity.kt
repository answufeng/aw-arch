package com.answufeng.arch.demo.loadstate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.answufeng.arch.demo.data.User
import com.answufeng.arch.demo.databinding.ActivityLoadStateListBinding
import com.answufeng.arch.demo.userlist.UserListAdapter
import com.answufeng.arch.ext.collectOnLifecycle
import com.answufeng.arch.mvvm.MvvmActivity
import com.answufeng.arch.state.LoadState

class LoadStateListActivity :
    MvvmActivity<ActivityLoadStateListBinding, LoadStateListViewModel>() {

    private val adapter = UserListAdapter { user ->
        Toast.makeText(this, "点击: ${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityLoadStateListBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadUsers() }
        binding.btnRetry.setOnClickListener { viewModel.retry() }
        binding.btnLoadSuccess.setOnClickListener { viewModel.loadUsersWithSuccess() }
        binding.btnLoadFail.setOnClickListener { viewModel.loadUsersWithFail() }
        binding.btnRefresh.setOnClickListener { viewModel.loadUsers() }

        viewModel.userLoadState.collectOnLifecycle(this) { state ->
            renderLoadState(state)
        }

        if (savedInstanceState == null) {
            viewModel.loadUsers()
        }
    }

    private fun renderLoadState(state: LoadState<List<User>>) {
        when (state) {
            is LoadState.Loading -> {
                binding.progressInitial.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = true
            }
            is LoadState.Success -> {
                binding.progressInitial.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                adapter.submitList(state.data)
                binding.tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
            }
            is LoadState.Error -> {
                binding.progressInitial.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.layoutError.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }
        }
    }
}
