package com.answufeng.arch.demo.userlist

import com.answufeng.arch.demo.data.MockRepository
import com.answufeng.arch.demo.data.User
import com.answufeng.arch.mvi.MviEffect
import com.answufeng.arch.mvi.MviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

data class UserListState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,       // 初始加载/刷新
    val isLoadingMore: Boolean = false,   // 加载更多
    val error: String? = null,            // 错误信息
    val hasMore: Boolean = true,          // 是否有更多数据
    val currentPage: Int = 0,             // 当前页码
) : UiState

sealed class UserListEffect : MviEffect {
    data class ShowToast(val message: String) : UserListEffect()
    data class ShowError(val message: String) : UserListEffect()
}

sealed class UserListIntent : UiIntent {
    data object LoadUsers : UserListIntent()          // 初始加载
    data object Refresh : UserListIntent()             // 下拉刷新
    data object LoadMore : UserListIntent()            // 加载更多
    data object Retry : UserListIntent()               // 错误重试
    data class UserClicked(val user: User) : UserListIntent()  // 点击用户
}

class UserListViewModel : MviViewModel<UserListState, UserListEffect, UserListIntent>(UserListState()) {

    companion object {
        private const val PAGE_SIZE = 6
        // 第 3 页模拟失败，用于演示错误重试
        private const val FAIL_PAGE = 3
    }

    private var hasFailedOnce = false

    override fun handleIntent(intent: UserListIntent) {
        when (intent) {
            UserListIntent.LoadUsers -> loadUsers()
            UserListIntent.Refresh -> refresh()
            UserListIntent.LoadMore -> loadMore()
            UserListIntent.Retry -> retry()
            is UserListIntent.UserClicked -> onUserClicked(intent.user)
        }
    }

    private fun loadUsers() = launchIO {
        if (currentState.users.isNotEmpty()) return@launchIO
        updateState { copy(isLoading = true, error = null) }
        fetchPage(1)
    }

    private fun refresh() = launchIO {
        updateState { copy(isLoading = true, error = null, currentPage = 0, hasMore = true) }
        hasFailedOnce = false
        fetchPage(1)
    }

    private fun loadMore() = launchIO {
        if (currentState.isLoadingMore || !currentState.hasMore) return@launchIO
        val nextPage = currentState.currentPage + 1
        updateState { copy(isLoadingMore = true) }
        fetchPage(nextPage)
    }

    private fun retry() = launchIO {
        val retryPage = currentState.currentPage + 1
        updateState { copy(isLoading = true, error = null) }
        fetchPage(retryPage)
    }

    private suspend fun fetchPage(page: Int) {
        try {
            // 第 FAIL_PAGE 页首次模拟失败
            val failPage = if (hasFailedOnce) -1 else FAIL_PAGE
            val users = MockRepository.fetchUsers(page, PAGE_SIZE, failPage)
            val noMore = users.size < PAGE_SIZE
            if (page == FAIL_PAGE && !hasFailedOnce) {
                hasFailedOnce = true
            }
            updateState {
                copy(
                    users = if (page == 1) users else this.users + users,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    currentPage = page,
                    hasMore = !noMore,
                )
            }
            if (page == 1 && users.isNotEmpty()) {
                sendMviEvent(UserListEffect.ShowToast("加载成功，共 ${users.size} 条"))
            }
        } catch (e: Exception) {
            updateState {
                copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "未知错误",
                )
            }
            sendMviEvent(UserListEffect.ShowError(e.message ?: "未知错误"))
        }
    }

    private fun onUserClicked(user: User) {
        sendMviEvent(UserListEffect.ShowToast("点击了: ${user.name}"))
    }
}
