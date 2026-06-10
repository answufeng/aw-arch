package com.answufeng.arch.mvi

import androidx.viewbinding.ViewBinding

/**
 * 简化版 MVI 架构 Fragment 基类
 *
 * 与 [MviFragment] 的区别在于不需要定义独立的 Event 类型，适用于不需要单向 UI 事件的简单场景。
 * 继承 [MviFragment]，复用 ViewBinding 生命周期管理与懒加载逻辑。
 * 支持懒加载，首次对用户可见时才调用 [MviFragment.onLazyLoad]。
 * 子类须声明第四类型参数 [VM]（具体 [SimpleMviViewModel] 实现），以便反射创建。
 *
 * @param VB ViewBinding 类型
 * @param STATE UI 状态类型，必须实现 [UiState]
 * @param INTENT UI 意图类型，必须实现 [UiIntent]
 * @param VM 必须继承 [SimpleMviViewModel] 且泛型为 [STATE]、[INTENT]
 *
 * @see SimpleMviViewModel
 * @see UiState
 * @see UiIntent
 * @see MviDispatcher
 */
abstract class SimpleMviFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > :
    MviFragment<VB, STATE, NoEvent, INTENT, VM>()
