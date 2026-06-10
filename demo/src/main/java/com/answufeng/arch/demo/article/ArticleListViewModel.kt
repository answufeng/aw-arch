package com.answufeng.arch.demo.article

import com.answufeng.arch.base.MvvmViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Article(
    val id: Int,
    val title: String,
    val summary: String,
    val author: String,
    val date: String,
)

class ArticleListViewModel : MvvmViewModel() {

    companion object {
        private val sampleArticles = listOf(
            Article(1, "Kotlin 协程入门指南", "从零开始学习 Kotlin 协程，理解 suspend、Job、CoroutineScope 等核心概念", "张三", "2026-06-01"),
            Article(2, "Android MVI 架构实践", "深入理解 MVI 架构模式，State/Effect/Intent 三层分离的最佳实践", "李四", "2026-05-28"),
            Article(3, "Jetpack Compose 状态管理", "Compose 中 remember、mutableStateOf、StateFlow 的使用场景与性能优化", "王五", "2026-05-25"),
            Article(4, "Hilt 依赖注入进阶", "Scope、Qualifier、Component 的深入理解与自定义扩展", "赵六", "2026-05-20"),
            Article(5, "RecyclerView 性能优化", "DiffUtil、Prefetch、ViewHolder 复用、缓存策略全解析", "孙七", "2026-05-18"),
            Article(6, "Android 启动优化实战", "从冷启动 3 秒到 800ms 的优化历程，涵盖 MultiDex、懒加载、预加载", "周八", "2026-05-15"),
            Article(7, "Flow 与 SharedFlow 详解", "冷流与热流的区别，StateFlow vs SharedFlow 的选型指南", "吴九", "2026-05-12"),
            Article(8, "Gradle 构建优化", "Configuration Cache、Build Cache、自定义 Plugin 提速构建", "郑十", "2026-05-10"),
        )
    }

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun loadArticles() = launchIO {
        showLoading(true)
        kotlinx.coroutines.delay(1000)
        _articles.value = sampleArticles
        showLoading(false)
    }

    fun search(query: String) = launchIO {
        if (query.isBlank()) {
            loadArticles()
            return@launchIO
        }
        _isSearching.value = true
        kotlinx.coroutines.delay(600)
        val result = sampleArticles.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.summary.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
        }
        _articles.value = result
        _isSearching.value = false
        if (result.isEmpty()) {
            showToast("未找到匹配的文章")
        }
    }

    fun clearSearch() {
        _isSearching.value = false
        launchIO { loadArticles() }
    }
}
