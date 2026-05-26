package com.answufeng.arch.mvp

import com.answufeng.arch.base.ArchView

/**
 * MVP 视图 Contract 标记接口，继承 [ArchView] 的通用 UI 能力。
 *
 * Presenter 通过该接口与 UI 层交互；子类可按需扩展更具体的 `Contract.View`。
 */
interface MvpView : ArchView
