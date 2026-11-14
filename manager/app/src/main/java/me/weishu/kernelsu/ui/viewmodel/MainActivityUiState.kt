package me.weishu.kernelsu.ui.viewmodel

import androidx.compose.runtime.Immutable
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val enableNavigationBadge: Boolean,
    val enableSwipeDismiss: Boolean,
    val pagerInterceptionMode: Int,
    val moduleDescriptionMaxLines: Int = 4,
    val uiMode: UiMode,
    // ---- Appearance / theme ----
    val customBackgroundUri: String = "",
    val customBackgroundUpdatedAt: Long = 0L,
    val customBackgroundBlurPercent: Int = 50,
    val customBackgroundScrimPercent: Int = -35,
    val cardBackgroundAlphaPercent: Int = 60,
    val enableProgressiveBlur: Boolean = false,
    val useSystemWallpaper: Boolean = false,
    val customTexts: Map<String, String> = emptyMap(),
)
