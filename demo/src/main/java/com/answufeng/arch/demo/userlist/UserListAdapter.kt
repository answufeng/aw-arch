package com.answufeng.arch.demo.userlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.arch.demo.data.User
import com.answufeng.arch.demo.databinding.ItemUserBinding

class UserListAdapter(
    private val onUserClick: (User) -> Unit,
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val items = mutableListOf<User>()

    fun submitList(newItems: List<User>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            // 显示首字母
            binding.tvAvatar.text = user.name.firstOrNull()?.toString() ?: ""
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.root.setOnClickListener { onUserClick(user) }
        }
    }
}
