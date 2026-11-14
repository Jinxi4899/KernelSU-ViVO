package me.weishu.kernelsu.ui.screen.settings

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.ImportedTheme
import me.weishu.kernelsu.ui.util.ResolvedAppearance

@Immutable
data class SettingsUiState(
    val uiMode: String = UiMode.DEFAULT_VALUE,
    val checkUpdate: Boolean = true,
    val checkModuleUpdate: Boolean = true,
    val themeMode: Int = 0,
    val miuixMonet: Boolean = false,
    val keyColor: Int = 0,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.SPEC_2025.name,
    val enablePredictiveBack: Boolean = false,
    val enableSwipeDismiss: Boolean = true,
    val pagerInterceptionMode: Int = 1,
    val enableBlur: Boolean = true,
    val enableFloatingBottomBar: Boolean = false,
    val enableFloatingBottomBarBlur: Boolean = false,
    val enableNavigationBadge: Boolean = true,
    val pageScale: Float = 1.0f,
    val moduleDescriptionMaxLines: Int = 4,
    val enableWebDebugging: Boolean = false,

    // ---- Theme appearance (SukiSU source names) ----
    val customBackgroundUri: String = "",
    val customBackgroundUpdatedAt: Long = 0L,
    val useSystemWallpaper: Boolean = false,
    val extractColorFromBackground: Boolean = false,
    val cardBackgroundAlphaPercent: Int = 60,
    val customBackgroundBlurPercent: Int = 50,
    val customBackgroundScrimPercent: Int = -35,
    val customHomeTitle: String = "",
    val customTexts: Map<String, String> = emptyMap(),
    /** BCP-47 language tag, or empty to follow the system language. */
    val appLanguage: String = "",
    val homeCardImagePath: String = "",
    val hideHomeCardStatusText: Boolean = false,
    val hideHomeCardDecoration: Boolean = false,
    val homeCardStickers: List<HomeCardSticker> = emptyList(),
    val bottomBarIcons: Map<String, String> = emptyMap(),
    val homeLayout: String = "classic",
    val homeStatusAlignment: String = "start",
    val homeStatusScale: Float = 1f,
    val homePillScale: Float = 1f,
    val homeStatusWidthFraction: Float = 1f,
    val homeCardImageStyle: String = "banner",
    val showHomeCardImageWithoutBackground: Boolean = false,
    val showFullStatus: Boolean = false,
    val hideHomeWarnings: Boolean = false,
    val adaptiveAppliedMode: Int = -1,
    val customAppearanceJson: String = "",
    val appIconId: String = "default",
    val appIconDynamicColor: Boolean = false,
    val enableProgressiveBlur: Boolean = false,
    val backgroundAppearanceExpanded: Boolean = true,
    val backgroundSectionExpanded: Boolean = true,
    val themePackageSectionExpanded: Boolean = true,
    val appearanceSectionExpanded: Boolean = true,
    val homeCardSectionExpanded: Boolean = true,
    val colorsSectionExpanded: Boolean = true,
    val bottomBarSectionExpanded: Boolean = true,
    val interactionSectionExpanded: Boolean = true,
    val detailsSectionExpanded: Boolean = true,

    // ---- Theme package workflows ----
    val pendingImportTheme: ImportedTheme? = null,
    val showThemeImportDialog: Boolean = false,
    val adaptiveThemeActive: Boolean = false,
    val adaptiveDraftComplete: Boolean = false,
    val appearance: ResolvedAppearance? = null,
    val appearanceData: AppearanceOverlayData? = null,
    val customColorsPresent: Boolean = false,
    val imageEffectsPresent: Boolean = false,
    val systemWallpaperPath: String = "",
    val isSystemWallpaperBusy: Boolean = false,

    // Su Compat
    val suCompatStatus: String = "",
    val suCompatMode: Int = 0, // 0: enable default, 1: disable until reboot, 2: disable always
    val isSuEnabled: Boolean = false,

    // Kernel Umount
    val kernelUmountStatus: String = "",
    val isKernelUmountEnabled: Boolean = false,

    // SELinux Hide
    val selinuxHideStatus: String = "",
    val isSelinuxHideEnabled: Boolean = false,

    // SU Log
    val sulogStatus: String = "",
    val isSulogEnabled: Boolean = false,

    // Umount Modules
    val isDefaultUmountModules: Boolean = false,

    // ADB Root
    val adbRootStatus: String = "",
    val isAdbRootEnabled: Boolean = false,

    // Shell SU ADB Only
    val shellSuAdbOnlyStatus: String = "",
    val isShellSuAdbOnlyEnabled: Boolean = false,

    val isLkmMode: Boolean = false,
    val isLateLoadMode: Boolean = false,

    // Auto Jailbreak
    val autoJailbreak: Boolean = false,

    // Soft Reboot
    val useSoftReboot: Boolean = false
)

@Immutable
data class SettingsScreenActions(
    val onSetCheckUpdate: (Boolean) -> Unit,
    val onSetCheckModuleUpdate: (Boolean) -> Unit,
    val onOpenTheme: () -> Unit,
    val onSetLanguage: (String) -> Unit,
    val onSetUiModeIndex: (Int) -> Unit,
    val onOpenProfileTemplate: () -> Unit,
    val onSetSuCompatMode: (Int) -> Unit,
    val onSetKernelUmountEnabled: (Boolean) -> Unit,
    val onSetSelinuxHideEnabled: (Boolean) -> Unit,
    val onSetSulogEnabled: (Boolean) -> Unit,
    val onSetAdbRootEnabled: (Boolean) -> Unit,
    val onSetShellSuAdbOnlyEnabled: (Boolean) -> Unit,
    val onSetDefaultUmountModules: (Boolean) -> Unit,
    val onSetEnableWebDebugging: (Boolean) -> Unit,
    val onSetAutoJailbreak: (Boolean) -> Unit,
    val onSetUseSoftReboot: (Boolean) -> Unit,
    val onOpenAbout: () -> Unit,
)
