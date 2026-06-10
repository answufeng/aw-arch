package com.answufeng.arch.mvi

import androidx.viewbinding.ViewBinding

/**
 * 简化版 MVI 架构 DialogFragment 基类
 *
 * 与 [SimpleMviFragment] 类似，但继承 [MviDialogFragment]，适用于对话框场景。
 * 不需要定义独立的 Event 类型；子类须声明第四类型参数 [VM]（具体 [SimpleMviViewModel]）。
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
abstract class SimpleMviDialogFragment<
    VB : ViewBinding,
    STATE : UiState,
    INTENT : UiIntent,
    VM : SimpleMviViewModel<STATE, INTENT>,
    > :
    MviDialogFragment<VB, STATE, NoEvent, INTENT, VM>()
