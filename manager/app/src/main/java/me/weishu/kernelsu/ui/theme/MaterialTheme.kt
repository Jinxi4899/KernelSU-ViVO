package me.weishu.kernelsu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.webui.MonetColorsProvider

/**
 * Wraps pages that can render inside the Miuix shell. In the Material shell the outer theme
 * already is MD3, so this only adds a nested theme under Miuix rather than re-deriving the
 * palette on every recomposition.
 */
@Composable
fun AiAssistantMaterialTheme(
    uiMode: UiMode = LocalUiMode.current,
    content: @Composable () -> Unit,
) {
    when (uiMode) {
        UiMode.Material -> content()
        UiMode.Miuix -> MaterialExpressivePalette(content)
    }
}

/**
 * Material 3 Expressive colours for pages that stay MD3 inside the Miuix shell (the theme
 * store, source management and assistant entry points). Same pipeline as
 * [MaterialKernelSUTheme] so light/dark, AMOLED, key colour and monet follow the manager's
 * appearance settings, without touching the window bars the outer shell already owns.
 */
@Composable
fun MaterialExpressivePalette(
    content: @Composable () -> Unit,
) {
    val appSettings = LocalAppSettings.current ?: ThemeController.getAppSettings()
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val colorScheme = rememberKernelSUColorScheme(
        seedColor = if (appSettings.keyColor == 0) Color.Unspecified else Color(appSettings.keyColor),
        isDark = darkTheme,
        isAmoled = appSettings.colorMode.isAmoled,
        paletteStyle = appSettings.paletteStyle,
        colorSpec = appSettings.colorSpec,
    )
    val overlayTokens = appSettings.appearance?.colorsFor(darkTheme)
    val overlaidScheme = remember(colorScheme, overlayTokens) {
        colorScheme.withAppearance(overlayTokens)
    }
    MaterialExpressiveTheme(
        colorScheme = overlaidScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = content,
    )
}

@Composable
fun MaterialKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val amoledMode = appSettings.colorMode.isAmoled
    val dynamicColor = appSettings.keyColor == 0

    val colorScheme = rememberKernelSUColorScheme(
        seedColor = if (dynamicColor) Color.Unspecified else Color(appSettings.keyColor),
        isDark = darkTheme,
        isAmoled = amoledMode,
        paletteStyle = appSettings.paletteStyle,
        colorSpec = appSettings.colorSpec,
    )

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val animatedColorScheme = colorScheme.animateAsState()

    val overlayTokens = appSettings.appearance?.colorsFor(darkTheme)
    val overlaidScheme = remember(animatedColorScheme, overlayTokens) {
        animatedColorScheme.withAppearance(overlayTokens)
    }
    // Frosted-glass pass while a theme background is active: every surface role resolves from
    // these tokens, so cards, search bars, dialogs and sheets blur the wallpaper in one pass.
    val hasCustomBackground = LocalHasCustomBackground.current
    val cardAlpha = LocalCardBackgroundAlpha.current
    val glassScheme = remember(overlaidScheme, hasCustomBackground, cardAlpha) {
        if (hasCustomBackground) overlaidScheme.withBackgroundGlass(cardAlpha) else overlaidScheme
    }

    MaterialExpressiveTheme(
        colorScheme = glassScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = {
            MonetColorsProvider.UpdateCss(glassScheme)
            content()
        }
    )
}

/**
 * Translucent card surfaces over a theme package background.
 *
 * Mirrors SukiSU: only the card roles ([ColorScheme.surfaceBright],
 * [ColorScheme.surfaceContainerLowest], [ColorScheme.surfaceContainerHighest]) go translucent.
 * Menu ([ColorScheme.surfaceContainer]), dialog (surfaceContainerHigh) and page (surface) roles
 * stay opaque so overlays keep their layering and remain readable over a bright wallpaper.
 */
private fun ColorScheme.withBackgroundGlass(alpha: Float): ColorScheme = copy(
    surfaceBright = surfaceBright.copy(alpha = alpha),
    surfaceContainerLowest = surfaceContainerLowest.copy(alpha = alpha),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = alpha),
)
