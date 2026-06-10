package com.answufeng.arch.demo.todo

import com.answufeng.arch.demo.data.MockRepository
import com.answufeng.arch.demo.data.TodoItem
import com.answufeng.arch.mvp.BaseMvpPresenter

class TodoPresenter : BaseMvpPresenter<TodoContract.View>(), TodoContract.Presenter {

    private val items = mutableListOf<TodoItem>()

    override fun loadTodos() = launchIO {
        withMain { viewOrNull?.showLoading(true) }
        try {
            val todos = MockRepository.fetchTodos()
            items.clear()
            items.addAll(todos)
            withMain {
                viewOrNull?.showTodos(items.toList())
                viewOrNull?.showStats(items.count { it.completed }, items.size)
                viewOrNull?.showEmpty(items.isEmpty())
            }
        } catch (e: Exception) {
            withMain { viewOrNull?.showError(e.message ?: "加载失败") }
        } finally {
            withMain { viewOrNull?.showLoading(false) }
        }
    }

    override fun addTodo(title: String) = launchIO {
        try {
            val item = MockRepository.addTodo(title)
            items.add(0, item)
            withMain {
                viewOrNull?.showTodos(items.toList())
                viewOrNull?.showStats(items.count { it.completed }, items.size)
                viewOrNull?.showEmpty(false)
                viewOrNull?.showAddSuccess(title)
            }
        } catch (e: Exception) {
            withMain { viewOrNull?.showError(e.message ?: "添加失败") }
        }
    }

    override fun toggleTodo(id: Int) = launchIO {
        try {
            val updated = MockRepository.toggleTodo(id)
            if (updated != null) {
                val index = items.indexOfFirst { it.id == id }
                if (index >= 0) items[index] = updated
                withMain {
                    viewOrNull?.showTodos(items.toList())
                    viewOrNull?.showStats(items.count { it.completed }, items.size)
                }
            }
        } catch (e: Exception) {
            withMain { viewOrNull?.showError(e.message ?: "操作失败") }
        }
    }

    override fun deleteTodo(id: Int) = launchIO {
        try {
            MockRepository.deleteTodo(id)
            items.removeAll { it.id == id }
            withMain {
                viewOrNull?.showTodos(items.toList())
                viewOrNull?.showStats(items.count { it.completed }, items.size)
                viewOrNull?.showEmpty(items.isEmpty())
            }
        } catch (e: Exception) {
            withMain { viewOrNull?.showError(e.message ?: "删除失败") }
        }
    }
}
