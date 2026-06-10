package com.answufeng.arch.demo.loadstate

import com.answufeng.arch.base.MvvmViewModel
import com.answufeng.arch.demo.data.MockRepository
import com.answufeng.arch.demo.data.User
import com.answufeng.arch.state.LoadState
import com.answufeng.arch.state.loadStateCatching
import com.answufeng.arch.state.retryLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoadStateListViewModel : MvvmViewModel() {

    private val _userLoadState = MutableStateFlow<LoadState<List<User>>>(LoadState.Loading)
    val userLoadState: StateFlow<LoadState<List<User>>> = _userLoadState.asStateFlow()

    private var shouldFail = false

    fun loadUsers() = launchIO {
        _userLoadState.value = LoadState.Loading
        kotlinx.coroutines.delay(800)
        val result = loadStateCatching {
            if (shouldFail) {
                throw RuntimeException("模拟网络错误")
            }
            MockRepository.fetchUsers(1, 20)
        }
        _userLoadState.value = result
    }

    fun loadUsersWithFail() {
        shouldFail = true
        loadUsers()
    }

    fun loadUsersWithSuccess() {
        shouldFail = false
        loadUsers()
    }

    fun retry() = launchIO {
        val result = retryLoadState(times = 2, initialDelayMillis = 500) {
            if (shouldFail) {
                shouldFail = false // 第二次重试时成功
            }
            MockRepository.fetchUsers(1, 20)
        }
        _userLoadState.value = result
    }
}
