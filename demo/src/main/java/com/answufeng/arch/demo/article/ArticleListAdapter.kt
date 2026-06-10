package com.answufeng.arch.demo.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.arch.demo.databinding.ItemArticleBinding

class ArticleListAdapter : RecyclerView.Adapter<ArticleListAdapter.ViewHolder>() {

    private val items = mutableListOf<Article>()

    fun submitList(newItems: List<Article>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTitle.text = article.title
            binding.tvSummary.text = article.summary
            binding.tvAuthor.text = article.author
            binding.tvDate.text = article.date
        }
    }
}
