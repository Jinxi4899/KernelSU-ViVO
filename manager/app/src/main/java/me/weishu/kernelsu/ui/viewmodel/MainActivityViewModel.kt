package me.weishu.kernelsu.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.theme.ThemeController
import me.weishu.kernelsu.ui.util.AdaptiveThemeStore
import me.weishu.kernelsu.ui.util.ImageStorage

class MainActivityViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val prefs = ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val settingRepo: SettingsRepository = SettingsRepositoryImpl()
    private val mainPageState = MainPageState(savedStateHandle)
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key in observedKeys) {
            _uiState.value = readUiState()
        }
    }

    private val _uiState = MutableStateFlow(readUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()
    val selectedMainPage: StateFlow<Int> = mainPageState.selectedPage

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun setSelectedMainPage(page: Int) {
        mainPageState.updateSelectedPage(page)
    }

    /**
     * Re-captures the launcher wallpaper when the user opted in. Android 13+ hides it from
     * WallpaperManager, so the file is copied through the root shell and only rewritten when
     * its signature changed.
     */
    fun refreshSystemWallpaper() {
        if (!settingRepo.useSystemWallpaper) return
        viewModelScope.launch(Dispatchers.IO) {
            val result = ImageStorage.captureSystemWallpaper(ksuApp)
            val path = result.path ?: return@launch
            if (result.changed) {
                settingRepo.customBackgroundUri = path
                settingRepo.customBackgroundUpdatedAt = System.currentTimeMillis()
            }
            if (settingRepo.extractColorFromBackground && settingRepo.keyColor == 0) {
                settingRepo.keyColor = ImageStorage.extractSeedColor(File(path), settingRepo.keyColor)
            }
        }
    }

    /** Swaps the adaptive theme half when the effective light/dark mode changed. */
    fun syncAdaptiveTheme(dark: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            AdaptiveThemeStore.sync(settingRepo, dark)
        }
    }

    private fun readUiState(): MainActivityUiState {
        return MainActivityUiState(
            appSettings = ThemeController.getAppSettings(settingRepo),
            pageScale = settingRepo.pageScale,
            enableBlur = settingRepo.enableBlur,
            enableFloatingBottomBar = settingRepo.enableFloatingBottomBar,
            enableFloatingBottomBarBlur = settingRepo.enableFloatingBottomBarBlur,
            enableNavigationBadge = settingRepo.enableNavigationBadge,
            enableSwipeDismiss = settingRepo.enableSwipeDismiss,
            pagerInterceptionMode = settingRepo.pagerInterceptionMode,
            moduleDescriptionMaxLines = settingRepo.moduleDescriptionMaxLines,
            uiMode = UiMode.fromValue(settingRepo.uiMode),
            customBackgroundUri = settingRepo.customBackgroundUri,
            customBackgroundUpdatedAt = settingRepo.customBackgroundUpdatedAt,
            customBackgroundBlurPercent = settingRepo.customBackgroundBlurPercent,
            customBackgroundScrimPercent = settingRepo.customBackgroundScrimPercent,
            cardBackgroundAlphaPercent = settingRepo.cardBackgroundAlphaPercent,
            enableProgressiveBlur = settingRepo.enableProgressiveBlur,
            useSystemWallpaper = settingRepo.useSystemWallpaper,
            customTexts = settingRepo.customTexts,
        )
    }

    private companion object {
        val observedKeys = setOf(
            "color_mode",
            "category_key_color",
            "key_color",
            "color_style",
            "color_spec",
            "page_scale",
            "module_description_max_lines",
            "enable_blur",
            "enable_floating_bottom_bar",
            "enable_floating_bottom_bar_blur",
            "enable_navigation_badge",
            "enable_swipe_dismiss",
            "pager_interception_mode",
            "ui_mode",
            "custom_bg_uri",
            "custom_bg_updated_at",
            "custom_bg_blur_percent",
            "custom_bg_scrim_percent",
            "card_bg_alpha_percent",
            "use_system_wallpaper",
            "extract_color_from_background",
            "custom_texts",
            "custom_home_title",
            "enable_progressive_blur",
            "adaptive_theme",
            "adaptive_applied_mode",
            "custom_appearance_json",
        )
    }
}

private const val SELECTED_MAIN_PAGE_KEY = "selected_main_page"

private class MainPageState(
    private val savedStateHandle: SavedStateHandle,
) {
    val selectedPage: StateFlow<Int> = savedStateHandle.getStateFlow(SELECTED_MAIN_PAGE_KEY, 0)

    fun updateSelectedPage(page: Int) {
        savedStateHandle[SELECTED_MAIN_PAGE_KEY] = MainPagerConfig.coercePage(page)
    }
}

object MainPagerConfig {
    const val PAGE_COUNT = 4
    const val LAST_PAGE_INDEX = PAGE_COUNT - 1

    fun coercePage(page: Int): Int = page.coerceIn(0, LAST_PAGE_INDEX)
}
