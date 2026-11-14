package me.weishu.kernelsu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.ui.util.HomeCardSticker

/**
 * Appearance snapshot consumed by the renderer.
 *
 * The repository stays the single source of truth; MainActivity builds this snapshot once per
 * refresh and provides it through [LocalThemeVisuals], so screens never read prefs directly.
 */
@Immutable
data class ThemeVisuals(
    val adaptive: Boolean = false,
    val backgroundBlurPercent: Int = DEFAULT_BACKGROUND_BLUR_PERCENT,
    val backgroundScrimPercent: Int = DEFAULT_BACKGROUND_SCRIM_PERCENT,
    val cardBackgroundAlphaPercent: Int = DEFAULT_CARD_ALPHA_PERCENT,
    val homeLayout: String = "classic",
    val homeStatusAlignment: String = "start",
    val homeStatusScale: Float = 1f,
    val homePillScale: Float = 1f,
    val homeStatusWidthFraction: Float = 1f,
    val hideHomeCardStatusText: Boolean = false,
    val hideHomeCardDecoration: Boolean = false,
    val homeCardImageStyle: String = "banner",
    val showHomeCardImageWithoutBackground: Boolean = false,
    val customHomeTitle: String = "",
    val customTexts: Map<String, String> = emptyMap(),
    val homeCardStickers: List<HomeCardSticker> = emptyList(),
    val bottomBarIcons: Map<String, String> = emptyMap(),
    val customAppearanceJson: String = "",
    val useSystemWallpaper: Boolean = false,
)

/** Defaults kept in sync with the repository (mirrors the SukiSU source manager). */
const val DEFAULT_BACKGROUND_BLUR_PERCENT = 50
const val DEFAULT_BACKGROUND_SCRIM_PERCENT = -35
const val DEFAULT_CARD_ALPHA_PERCENT = 60

val LocalThemeVisuals = staticCompositionLocalOf { ThemeVisuals() }

/** Root scaffold transparency switch: a custom background is active. */
val LocalHasCustomBackground = staticCompositionLocalOf { false }

/** Card surface alpha (0..1) used by the glass bars over the wallpaper. */
val LocalCardBackgroundAlpha = staticCompositionLocalOf { DEFAULT_CARD_ALPHA_PERCENT / 100f }

/** Progressive (edge-gradient) blur behind the top and bottom bars. */
val LocalEnableProgressiveBlur = staticCompositionLocalOf { false }

/** Theme-supplied text overrides keyed by the SukiSU custom-text keys. */
val LocalCustomTexts = staticCompositionLocalOf<Map<String, String>> { emptyMap() }

/** Builds the snapshot from the repository. */
fun themeVisualsFrom(repo: SettingsRepository): ThemeVisuals = ThemeVisuals(
    adaptive = repo.adaptiveThemeJson.isNotBlank(),
    backgroundBlurPercent = repo.customBackgroundBlurPercent,
    backgroundScrimPercent = repo.customBackgroundScrimPercent,
    cardBackgroundAlphaPercent = repo.cardBackgroundAlphaPercent,
    homeLayout = repo.homeLayout,
    homeStatusAlignment = repo.homeStatusAlignment,
    homeStatusScale = repo.homeStatusScale,
    homePillScale = repo.homePillScale,
    homeStatusWidthFraction = repo.homeStatusWidthFraction,
    hideHomeCardStatusText = repo.hideHomeCardStatusText,
    hideHomeCardDecoration = repo.hideHomeCardDecoration,
    homeCardImageStyle = repo.homeCardImageStyle,
    showHomeCardImageWithoutBackground = repo.showHomeCardImageWithoutBackground,
    customHomeTitle = repo.customHomeTitle,
    customTexts = repo.customTexts,
    homeCardStickers = repo.homeCardStickers,
    bottomBarIcons = repo.bottomBarIcons,
    customAppearanceJson = repo.customAppearanceJson,
    useSystemWallpaper = repo.useSystemWallpaper,
)

/**
 * Key color and [SettingsRepository.extractColorFromBackground] are mutually exclusive:
 * an explicit color always wins, so callers must read through this helper.
 */
fun SettingsRepository.derivesKeyColorFromBackground(): Boolean =
    extractColorFromBackground && keyColor == 0

/**
 * Custom text replacement: falls back to [default] when no key has a custom value.
 * [fallbackKeys] let a page inherit text authored for a legacy key.
 */
@Composable
fun themeCustomText(key: String?, default: String, vararg fallbackKeys: String?): String {
    val texts = LocalCustomTexts.current
    if (key != null) {
        texts[key]?.takeIf { it.isNotBlank() }?.let { return it }
    }
    fallbackKeys.forEach { alias ->
        if (alias != null) {
            texts[alias]?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    return default
}
