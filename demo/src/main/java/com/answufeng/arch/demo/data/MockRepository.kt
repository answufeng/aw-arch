package com.answufeng.arch.demo.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/**
 * 模拟用户数据
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String,
)

/**
 * 模拟任务数据
 */
data class TodoItem(
    val id: Int,
    val title: String,
    val completed: Boolean = false,
)

/**
 * 模拟数据仓库，使用 delay 模拟网络请求
 */
object MockRepository {

    private val users = listOf(
        User(1, "张三", "zhangsan@example.com", "Z"),
        User(2, "李四", "lisi@example.com", "L"),
        User(3, "王五", "wangwu@example.com", "W"),
        User(4, "赵六", "zhaoliu@example.com", "Z"),
        User(5, "孙七", "sunqi@example.com", "S"),
        User(6, "周八", "zhouba@example.com", "Z"),
        User(7, "吴九", "wujiu@example.com", "W"),
        User(8, "郑十", "zhengshi@example.com", "Z"),
        User(9, "陈一", "chenyi@example.com", "C"),
        User(10, "林二", "liner@example.com", "L"),
        User(11, "黄三", "huangsan@example.com", "H"),
        User(12, "杨四", "yangsi@example.com", "Y"),
        User(13, "何五", "hewu@example.com", "H"),
        User(14, "马六", "maliu@example.com", "M"),
        User(15, "罗七", "luoqi@example.com", "L"),
        User(16, "梁八", "liangba@example.com", "L"),
        User(17, "宋九", "songjiu@example.com", "S"),
        User(18, "唐十", "tangshi@example.com", "T"),
        User(19, "许一", "xuyi@example.com", "X"),
        User(20, "韩二", "haner@example.com", "H"),
    )

    private val todos = mutableListOf(
        TodoItem(1, "完成项目架构设计", true),
        TodoItem(2, "编写单元测试", false),
        TodoItem(3, "代码审查", false),
        TodoItem(4, "更新文档", true),
        TodoItem(5, "修复登录页 Bug", false),
        TodoItem(6, "优化列表加载性能", false),
        TodoItem(7, "集成 CI/CD 流水线", true),
        TodoItem(8, "准备技术分享 PPT", false),
    )

    private var nextTodoId = 9

    /**
     * 获取用户列表（分页），模拟网络延迟
     *
     * @param page 页码（从 1 开始）
     * @param pageSize 每页数量
     * @param failOnPage 在指定页码模拟失败（用于演示错误重试）
     * @return 用户列表，如果已无更多数据返回空列表
     */
    suspend fun fetchUsers(
        page: Int,
        pageSize: Int = 6,
        failOnPage: Int = -1,
    ): List<User> {
        delay(800 + (page * 200L)) // 模拟越往后延迟越长
        coroutineContext.ensureActive()

        if (failOnPage > 0 && page == failOnPage) {
            delay(300)
            throw RuntimeException("网络连接超时，请重试 (page=$page)")
        }

        val from = (page - 1) * pageSize
        if (from >= users.size) return emptyList()
        return users.subList(from, minOf(from + pageSize, users.size))
    }

    /**
     * 搜索用户
     */
    suspend fun searchUsers(query: String): List<User> {
        delay(500)
        coroutineContext.ensureActive()
        if (query.isBlank()) return emptyList()
        return users.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
        }
    }

    /**
     * 获取任务列表
     */
    suspend fun fetchTodos(): List<TodoItem> {
        delay(600)
        coroutineContext.ensureActive()
        return todos.toList()
    }

    /**
     * 添加任务
     */
    suspend fun addTodo(title: String): TodoItem {
        delay(400)
        coroutineContext.ensureActive()
        val item = TodoItem(nextTodoId++, title)
        todos.add(0, item)
        return item
    }

    /**
     * 切换任务完成状态
     */
    suspend fun toggleTodo(id: Int): TodoItem? {
        delay(200)
        coroutineContext.ensureActive()
        val index = todos.indexOfFirst { it.id == id }
        if (index < 0) return null
        val updated = todos[index].copy(completed = !todos[index].completed)
        todos[index] = updated
        return updated
    }

    /**
     * 删除任务
     */
    suspend fun deleteTodo(id: Int): Boolean {
        delay(300)
        coroutineContext.ensureActive()
        return todos.removeAll { it.id == id }
    }
}
