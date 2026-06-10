package com.answufeng.arch.demo.todo

import com.answufeng.arch.demo.data.TodoItem
import com.answufeng.arch.mvp.MvpPresenter
import com.answufeng.arch.mvp.MvpView

interface TodoContract {

    interface View : MvpView {
        fun showLoading(show: Boolean)
        fun showTodos(items: List<TodoItem>)
        fun showStats(completed: Int, total: Int)
        fun showEmpty(show: Boolean)
        fun showAddSuccess(title: String)
        fun showError(message: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadTodos()
        fun addTodo(title: String)
        fun toggleTodo(id: Int)
        fun deleteTodo(id: Int)
    }
}
