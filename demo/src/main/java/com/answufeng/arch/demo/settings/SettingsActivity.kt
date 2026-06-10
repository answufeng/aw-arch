package com.answufeng.arch.demo.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.answufeng.arch.demo.databinding.ActivitySettingsBinding
import com.answufeng.arch.mvi.SimpleMviActivity

class SettingsActivity :
    SimpleMviActivity<ActivitySettingsBinding, SettingsState, SettingsIntent, SettingsViewModel>() {

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivitySettingsBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.topBar.setNavigationOnClickListener { finish() }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleNotification(isChecked))
        }
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleDarkMode(isChecked))
        }
        binding.switchAutoUpdate.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleAutoUpdate(isChecked))
        }
        binding.btnClearCache.setOnClickListener { dispatch(SettingsIntent.ClearCache) }
        binding.btnSaveSettings.setOnClickListener { dispatch(SettingsIntent.SaveSettings) }

        // 初始加载缓存大小
        if (savedInstanceState == null) {
            dispatch(SettingsIntent.CalculateCache)
        }
    }

    override fun render(state: SettingsState) {
        // 同步 Switch 状态（避免递归，先移除监听器再设置）
        binding.switchNotification.setOnCheckedChangeListener(null)
        binding.switchNotification.isChecked = state.notificationEnabled
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleNotification(isChecked))
        }

        binding.switchDarkMode.setOnCheckedChangeListener(null)
        binding.switchDarkMode.isChecked = state.darkModeEnabled
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleDarkMode(isChecked))
        }

        binding.switchAutoUpdate.setOnCheckedChangeListener(null)
        binding.switchAutoUpdate.isChecked = state.autoUpdateEnabled
        binding.switchAutoUpdate.setOnCheckedChangeListener { _, isChecked ->
            dispatch(SettingsIntent.ToggleAutoUpdate(isChecked))
        }

        // 缓存大小
        binding.tvCacheSize.text = "缓存大小: ${state.cacheSize}"

        // 保存状态
        binding.btnSaveSettings.isEnabled = !state.isSaving
        binding.tvSaveStatus.visibility = if (state.saveSuccess != null || state.isSaving) View.VISIBLE else View.GONE
        binding.tvSaveStatus.text = when {
            state.isSaving -> "保存中..."
            state.saveSuccess == true -> "设置已保存"
            state.saveSuccess == false -> "保存失败，请重试"
            else -> ""
        }
    }
}
