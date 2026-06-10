package com.answufeng.arch.demo.todo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.data.TodoItem
import com.answufeng.arch.demo.databinding.ItemTodoBinding

class TodoListAdapter(
    private val onToggle: (id: Int) -> Unit,
    private val onDelete: (id: Int) -> Unit,
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    private val items = mutableListOf<TodoItem>()

    fun submitList(newItems: List<TodoItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TodoItem) {
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = item.completed
            binding.tvTitle.text = item.title

            if (item.completed) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.setTextColor(itemView.context.getColor(R.color.aw_demo_on_surface_muted))
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.setTextColor(itemView.context.getColor(R.color.aw_demo_on_surface))
            }

            binding.cbCompleted.setOnCheckedChangeListener { _, _ ->
                onToggle(item.id)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(item.id)
            }
        }
    }
}
