package com.answufeng.arch.mvi

import androidx.viewbinding.ViewBinding

/**
 * 简化版 MVI 架构 Activity 基类
 *
 * 与 [MviActivity] 的区别在于不需要定义独立的 Event 类型，适用于不需要单向 UI 事件的简单场景。
 * 子类须声明 `SimpleMviActivity<VB, STATE, INTENT, VM>`，其中 [VM] 为具体的 [SimpleMviViewModel] 实现类型，供反射创建。
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
abstract class SimpleMviActivity<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > :
    MviActivity<VB, STATE, NoEvent, INTENT, VM>()
