package com.answufeng.arch.demo.settings

import com.answufeng.arch.mvi.SimpleMviViewModel
import com.answufeng.arch.mvi.UiIntent
import com.answufeng.arch.mvi.UiState

data class SettingsState(
    val notificationEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoUpdateEnabled: Boolean = true,
    val cacheSize: String = "计算中...",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean? = null,  // null=未保存, true=成功, false=失败
) : UiState

sealed class SettingsIntent : UiIntent {
    data class ToggleNotification(val enabled: Boolean) : SettingsIntent()
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent()
    data class ToggleAutoUpdate(val enabled: Boolean) : SettingsIntent()
    data object CalculateCache : SettingsIntent()
    data object ClearCache : SettingsIntent()
    data object SaveSettings : SettingsIntent()
}

class SettingsViewModel : SimpleMviViewModel<SettingsState, SettingsIntent>(SettingsState()) {

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleNotification -> updateState { copy(notificationEnabled = intent.enabled) }
            is SettingsIntent.ToggleDarkMode -> updateState { copy(darkModeEnabled = intent.enabled) }
            is SettingsIntent.ToggleAutoUpdate -> updateState { copy(autoUpdateEnabled = intent.enabled) }
            SettingsIntent.CalculateCache -> calculateCache()
            SettingsIntent.ClearCache -> clearCache()
            SettingsIntent.SaveSettings -> saveSettings()
        }
    }

    private fun calculateCache() = launchIO {
        updateState { copy(cacheSize = "计算中...") }
        kotlinx.coroutines.delay(800)
        val size = (Math.random() * 100 + 10).toInt()
        updateState { copy(cacheSize = "${size}MB") }
    }

    private fun clearCache() = launchIO {
        updateState { copy(cacheSize = "清除中...") }
        kotlinx.coroutines.delay(600)
        updateState { copy(cacheSize = "0MB") }
    }

    private fun saveSettings() = launchIO {
        updateState { copy(isSaving = true, saveSuccess = null) }
        kotlinx.coroutines.delay(1000)
        // 模拟 80% 成功率
        val success = Math.random() > 0.2
        updateState { copy(isSaving = false, saveSuccess = success) }
    }
}
