package com.answufeng.arch.demo.hub

import android.content.Intent
import android.os.Bundle
import com.answufeng.arch.demo.BaseMenuActivity
import com.answufeng.arch.demo.MviDemoActivity
import com.answufeng.arch.demo.MvpDemoActivity
import com.answufeng.arch.demo.MvvmDemoActivity
import com.answufeng.arch.demo.R
import com.answufeng.arch.demo.SimpleMviDemoActivity
import com.answufeng.arch.demo.article.ArticleListActivity
import com.answufeng.arch.demo.settings.SettingsActivity
import com.answufeng.arch.demo.todo.TodoListActivity
import com.answufeng.arch.demo.userlist.UserListActivity

class ArchPatternMenuActivity : BaseMenuActivity() {
    override val pageTitle: String get() = getString(R.string.demo_hub_arch_title)
    override val pageDesc: String get() = getString(R.string.demo_hub_arch_desc)

    override fun buildMenu(savedInstanceState: Bundle?) {
        addMenuItem("MVVM", subtitle = "ViewModel + UiEvent", intent = Intent(this, MvvmDemoActivity::class.java))
        addMenuItem("MVVM · 文章列表", subtitle = "搜索 + RecyclerView + StateFlow", intent = Intent(this, ArticleListActivity::class.java))
        addMenuItem("MVI", subtitle = "State / Effect / Intent", intent = Intent(this, MviDemoActivity::class.java))
        addMenuItem("MVI · 用户列表", subtitle = "下拉刷新 + 加载更多 + 错误重试", intent = Intent(this, UserListActivity::class.java))
        addMenuItem(
            "SimpleMVI",
            subtitle = "无独立 Effect 通道",
            outlined = true,
            intent = Intent(this, SimpleMviDemoActivity::class.java),
        )
        addMenuItem("SimpleMVI · 设置页", subtitle = "开关 + 数据保存 + 缓存管理", outlined = true, intent = Intent(this, SettingsActivity::class.java))
        addMenuItem("MVP", subtitle = "Contract + Presenter", outlined = true, intent = Intent(this, MvpDemoActivity::class.java))
        addMenuItem("MVP · 待办事项", subtitle = "增删改查 + CheckBox + 统计", outlined = true, intent = Intent(this, TodoListActivity::class.java))
    }
}
