package com.answufeng.arch.demo.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.answufeng.arch.demo.databinding.ActivityTodoListBinding
import com.answufeng.arch.mvp.MvpActivity

class TodoListActivity :
    MvpActivity<ActivityTodoListBinding, TodoContract.View, TodoPresenter>(),
    TodoContract.View {

    private val adapter = TodoListAdapter(
        onToggle = { id -> presenter.toggleTodo(id) },
        onDelete = { id -> presenter.deleteTodo(id) },
    )

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityTodoListBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }
        binding.recyclerView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val title = binding.etNewTask.text?.toString()?.trim() ?: ""
            if (title.isNotBlank()) {
                presenter.addTodo(title)
                binding.etNewTask.text?.clear()
            } else {
                Toast.makeText(this, "请输入任务内容", Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState == null) {
            presenter.loadTodos()
        }
    }

    override fun showLoading(show: Boolean) {
        binding.progressInitial.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showTodos(items: List<com.answufeng.arch.demo.data.TodoItem>) {
        adapter.submitList(items)
    }

    override fun showStats(completed: Int, total: Int) {
        binding.tvStats.text = "已完成 $completed / 共 $total 项"
    }

    override fun showEmpty(show: Boolean) {
        binding.tvEmpty.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showAddSuccess(title: String) {
        Toast.makeText(this, "已添加: $title", Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, "错误: $message", Toast.LENGTH_LONG).show()
    }
}
