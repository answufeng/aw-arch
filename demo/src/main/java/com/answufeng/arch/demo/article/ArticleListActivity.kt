package com.answufeng.arch.demo.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.answufeng.arch.demo.databinding.ActivityArticleListBinding
import com.answufeng.arch.ext.collectOnLifecycle
import com.answufeng.arch.mvvm.MvvmActivity

class ArticleListActivity : MvvmActivity<ActivityArticleListBinding, ArticleListViewModel>() {

    private val adapter = ArticleListAdapter()

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityArticleListBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }
        binding.recyclerView.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text?.toString()?.trim() ?: ""
            if (query.isNotBlank()) {
                viewModel.search(query)
            } else {
                showToast("请输入搜索关键词")
            }
        }

        binding.btnClear.setOnClickListener {
            binding.etSearch.text?.clear()
            viewModel.clearSearch()
        }

        binding.btnRetry.setOnClickListener { viewModel.loadArticles() }

        // 观察文章列表
        viewModel.articles.collectOnLifecycle(this) { articles ->
            adapter.submitList(articles)
            binding.tvEmpty.visibility = if (articles.isEmpty()) View.VISIBLE else View.GONE
            binding.layoutError.visibility = View.GONE
        }

        // 初始加载
        if (savedInstanceState == null) {
            viewModel.loadArticles()
        }
    }

    override fun onLoading(show: Boolean) {
        binding.progressInitial.visibility = if (show) View.VISIBLE else View.GONE
    }
}
