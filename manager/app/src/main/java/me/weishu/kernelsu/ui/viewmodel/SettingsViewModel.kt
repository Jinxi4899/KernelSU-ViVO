package me.weishu.kernelsu.ui.viewmodel

import android.net.Uri
import android.system.OsConstants
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState
import me.weishu.kernelsu.ui.screen.settings.ThemePackageOptions
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.themepackage.ThemePackageManager
import me.weishu.kernelsu.ui.util.AdaptiveThemeStore
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.AppearanceOverlayStore
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.ThemeTrialManager

class SettingsViewModel(
    private val repo: SettingsRepository = SettingsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val packages = ThemePackageManager(ksuApp, repo)

    init {
        ThemeTrialManager.cleanupOrphan(ksuApp)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val checkUpdate = repo.checkUpdate
            val checkModuleUpdate = repo.checkModuleUpdate
            val themeMode = repo.themeMode
            val miuixMonet = repo.miuixMonet
            val keyColor = repo.keyColor
            val enablePredictiveBack = repo.enablePredictiveBack
            val enableSwipeDismiss = repo.enableSwipeDismiss
            val pagerInterceptionMode = repo.pagerInterceptionMode
            val enableBlur = repo.enableBlur
            val enableFloatingBottomBar = repo.enableFloatingBottomBar
            val enableFloatingBottomBarBlur = repo.enableFloatingBottomBarBlur
            val enableNavigationBadge = repo.enableNavigationBadge
            val pageScale = repo.pageScale
            val moduleDescriptionMaxLines = repo.moduleDescriptionMaxLines
            val enableWebDebugging = repo.enableWebDebugging
            val colorStyle = repo.colorStyle
            val colorSpec = repo.colorSpec
            val isLkmMode = repo.isLkmMode()

            // Async loading for natives/features
            val suCompatStatus = repo.getSuCompatStatus()
            val suCompatPersistValue = repo.getSuCompatPersistValue()
            val isSuEnabled = repo.isSuEnabled()

            // Forced features show the value in effect, the persisted one is ignored
            val suCompatMode = when {
                suCompatStatus == "forced" -> if (isSuEnabled) 0 else 2
                suCompatPersistValue == 0L -> 2
                !isSuEnabled -> 1
                else -> 0
            }

            val kernelUmountStatus = repo.getKernelUmountStatus()
            val isKernelUmountEnabled = repo.isKernelUmountEnabled()
            val selinuxHideStatus = repo.getSelinuxHideStatus()
            val isSelinuxHideEnabled = repo.isSelinuxHideEnabled()
            val sulogStatus = repo.getSulogStatus()
            val isSulogEnabled = if (sulogStatus == "forced") {
                repo.getSulogValue() == 1L
            } else {
                repo.getSulogPersistValue() == 1L
            }
            val adbRootStatus = repo.getAdbRootStatus()
            val isAdbRootEnabled = if (adbRootStatus == "forced") {
                repo.getAdbRootValue() == 1L
            } else {
                repo.getAdbRootPersistValue() == 1L
            }
            val shellSuAdbOnlyStatus = repo.getShellSuAdbOnlyStatus()
            val isShellSuAdbOnlyEnabled = if (shellSuAdbOnlyStatus == "forced") {
                repo.getShellSuAdbOnlyValue() == 1L
            } else {
                repo.getShellSuAdbOnlyPersistValue() == 1L
            }
            val isDefaultUmountModules = repo.isDefaultUmountModules()
            val uiMode = repo.uiMode
            val autoJailbreak = repo.autoJailbreak
            val useSoftReboot = repo.useSoftReboot
            val isLateLoadMode = Natives.isLateLoadMode

            // Theme appearance snapshot (repo is the single source of truth).
            val customBackgroundUri = repo.customBackgroundUri
            val customBackgroundUpdatedAt = repo.customBackgroundUpdatedAt
            val useSystemWallpaper = repo.useSystemWallpaper
            val extractColorFromBackground = repo.extractColorFromBackground
            val cardBackgroundAlphaPercent = repo.cardBackgroundAlphaPercent
            val customBackgroundBlurPercent = repo.customBackgroundBlurPercent
            val customBackgroundScrimPercent = repo.customBackgroundScrimPercent
            val customHomeTitle = repo.customHomeTitle
            val customTexts = repo.customTexts
            val appLanguage = repo.appLanguage
            val homeCardImagePath = repo.homeCardImagePath
            val hideHomeCardStatusText = repo.hideHomeCardStatusText
            val hideHomeCardDecoration = repo.hideHomeCardDecoration
            val homeCardStickers = repo.homeCardStickers
            val bottomBarIcons = repo.bottomBarIcons
            val homeLayout = repo.homeLayout
            val homeStatusAlignment = repo.homeStatusAlignment
            val homeStatusScale = repo.homeStatusScale
            val homePillScale = repo.homePillScale
            val homeStatusWidthFraction = repo.homeStatusWidthFraction
            val homeCardImageStyle = repo.homeCardImageStyle
            val showHomeCardImageWithoutBackground = repo.showHomeCardImageWithoutBackground
            val showFullStatus = repo.showFullStatus
            val hideHomeWarnings = repo.hideHomeWarnings
            val adaptiveAppliedMode = repo.adaptiveAppliedMode
            val customAppearanceJson = repo.customAppearanceJson
            val appIconId = repo.appIconId
            val appIconDynamicColor = repo.appIconDynamicColor
            val enableProgressiveBlur = repo.enableProgressiveBlur
            val backgroundAppearanceExpanded = repo.backgroundAppearanceExpanded
            val backgroundSectionExpanded = repo.backgroundSectionExpanded
            val themePackageSectionExpanded = repo.themePackageSectionExpanded
            val appearanceSectionExpanded = repo.appearanceSectionExpanded
            val homeCardSectionExpanded = repo.homeCardSectionExpanded
            val colorsSectionExpanded = repo.colorsSectionExpanded
            val bottomBarSectionExpanded = repo.bottomBarSectionExpanded
            val interactionSectionExpanded = repo.interactionSectionExpanded
            val detailsSectionExpanded = repo.detailsSectionExpanded
            val appearanceData = withContext(Dispatchers.IO) { AppearanceOverlayStore.data(repo) }
            val appearance = AppearanceOverlayStore.load(repo)
            val adaptiveThemeActive = AdaptiveThemeStore.isActive(repo)
            val adaptiveDraftComplete = AdaptiveThemeStore.loadDraft(repo)?.complete == true
            val systemWallpaperPath = ImageStorage.systemWallpaperFile(ksuApp)
                .takeIf { it.isFile }?.absolutePath.orEmpty()

            _uiState.update {
                it.copy(
                    uiMode = uiMode,
                    checkUpdate = checkUpdate,
                    checkModuleUpdate = checkModuleUpdate,
                    themeMode = themeMode,
                    miuixMonet = miuixMonet,
                    keyColor = keyColor,
                    enablePredictiveBack = enablePredictiveBack,
                    enableSwipeDismiss = enableSwipeDismiss,
                    pagerInterceptionMode = pagerInterceptionMode,
                    enableBlur = enableBlur,
                    enableFloatingBottomBar = enableFloatingBottomBar,
                    enableFloatingBottomBarBlur = enableFloatingBottomBarBlur,
                    enableNavigationBadge = enableNavigationBadge,
                    pageScale = pageScale,
                    moduleDescriptionMaxLines = moduleDescriptionMaxLines,
                    enableWebDebugging = enableWebDebugging,
                    colorStyle = colorStyle,
                    colorSpec = colorSpec,
                    customBackgroundUri = customBackgroundUri,
                    customBackgroundUpdatedAt = customBackgroundUpdatedAt,
                    useSystemWallpaper = useSystemWallpaper,
                    extractColorFromBackground = extractColorFromBackground,
                    cardBackgroundAlphaPercent = cardBackgroundAlphaPercent,
                    customBackgroundBlurPercent = customBackgroundBlurPercent,
                    customBackgroundScrimPercent = customBackgroundScrimPercent,
                    customHomeTitle = customHomeTitle,
                    customTexts = customTexts,
                    appLanguage = appLanguage,
                    homeCardImagePath = homeCardImagePath,
                    hideHomeCardStatusText = hideHomeCardStatusText,
                    hideHomeCardDecoration = hideHomeCardDecoration,
                    homeCardStickers = homeCardStickers,
                    bottomBarIcons = bottomBarIcons,
                    homeLayout = homeLayout,
                    homeStatusAlignment = homeStatusAlignment,
                    homeStatusScale = homeStatusScale,
                    homePillScale = homePillScale,
                    homeStatusWidthFraction = homeStatusWidthFraction,
                    homeCardImageStyle = homeCardImageStyle,
                    showHomeCardImageWithoutBackground = showHomeCardImageWithoutBackground,
                    showFullStatus = showFullStatus,
                    hideHomeWarnings = hideHomeWarnings,
                    adaptiveAppliedMode = adaptiveAppliedMode,
                    customAppearanceJson = customAppearanceJson,
                    appIconId = appIconId,
                    appIconDynamicColor = appIconDynamicColor,
                    enableProgressiveBlur = enableProgressiveBlur,
                    backgroundAppearanceExpanded = backgroundAppearanceExpanded,
                    backgroundSectionExpanded = backgroundSectionExpanded,
                    themePackageSectionExpanded = themePackageSectionExpanded,
                    appearanceSectionExpanded = appearanceSectionExpanded,
                    homeCardSectionExpanded = homeCardSectionExpanded,
                    colorsSectionExpanded = colorsSectionExpanded,
                    bottomBarSectionExpanded = bottomBarSectionExpanded,
                    interactionSectionExpanded = interactionSectionExpanded,
                    detailsSectionExpanded = detailsSectionExpanded,
                    appearanceData = appearanceData,
                    appearance = appearance,
                    customColorsPresent = appearanceData?.colors != null,
                    imageEffectsPresent = appearanceData?.image != null,
                    adaptiveThemeActive = adaptiveThemeActive,
                    adaptiveDraftComplete = adaptiveDraftComplete,
                    systemWallpaperPath = systemWallpaperPath,
                    pendingImportTheme = _uiState.value.pendingImportTheme,
                    showThemeImportDialog = _uiState.value.showThemeImportDialog,
                    isSystemWallpaperBusy = _uiState.value.isSystemWallpaperBusy,
                    suCompatStatus = suCompatStatus,
                    suCompatMode = suCompatMode,
                    isSuEnabled = isSuEnabled,
                    adbRootStatus = adbRootStatus,
                    isAdbRootEnabled = isAdbRootEnabled,
                    shellSuAdbOnlyStatus = shellSuAdbOnlyStatus,
                    isShellSuAdbOnlyEnabled = isShellSuAdbOnlyEnabled,
                    kernelUmountStatus = kernelUmountStatus,
                    isKernelUmountEnabled = isKernelUmountEnabled,
                    selinuxHideStatus = selinuxHideStatus,
                    isSelinuxHideEnabled = isSelinuxHideEnabled,
                    sulogStatus = sulogStatus,
                    isSulogEnabled = isSulogEnabled,
                    isDefaultUmountModules = isDefaultUmountModules,
                    isLkmMode = isLkmMode,
                    autoJailbreak = autoJailbreak,
                    useSoftReboot = useSoftReboot,
                    isLateLoadMode = isLateLoadMode,
                )
            }
        }
    }

    fun setCheckUpdate(enabled: Boolean) {
        repo.checkUpdate = enabled
        _uiState.update { it.copy(checkUpdate = enabled) }
    }

    fun setUiMode(mode: String) {
        val oldMode = repo.uiMode
        val currentThemeMode = repo.themeMode

        val newThemeMode = when (oldMode) {
            "material" if mode == "miuix" -> {
                val colorMode = ColorMode.fromValue(currentThemeMode)
                val baseMode = if (colorMode == ColorMode.DARK_AMOLED) 2 else currentThemeMode
                if (repo.miuixMonet && !colorMode.isMonet) {
                    ColorMode.fromValue(baseMode).toMonetMode()
                } else if (!repo.miuixMonet && colorMode.isMonet) {
                    ColorMode.fromValue(baseMode).toNonMonetMode()
                } else baseMode
            }

            "miuix" if mode == "material" -> {
                val colorMode = ColorMode.fromValue(currentThemeMode)
                if (colorMode.isMonet) {
                    colorMode.toNonMonetMode()
                } else currentThemeMode
            }

            else -> currentThemeMode
        }

        repo.uiMode = mode
        repo.themeMode = newThemeMode
        _uiState.update { it.copy(uiMode = mode, themeMode = newThemeMode) }
    }

    fun setCheckModuleUpdate(enabled: Boolean) {
        repo.checkModuleUpdate = enabled
        _uiState.update { it.copy(checkModuleUpdate = enabled) }
    }

    fun setThemeMode(mode: Int) {
        val currentUiMode = repo.uiMode
        val effectiveMode = if (currentUiMode == "miuix" && _uiState.value.miuixMonet) {
            mode + 3
        } else {
            mode
        }
        repo.themeMode = effectiveMode
        _uiState.update { it.copy(themeMode = effectiveMode) }
    }

    fun setColorMode(mode: ColorMode) {
        repo.themeMode = mode.value
        _uiState.update { it.copy(themeMode = mode.value) }
    }

    fun setMiuixMonet(enabled: Boolean) {
        val currentThemeMode = repo.themeMode
        val colorMode = ColorMode.fromValue(currentThemeMode)
        val newThemeMode = if (enabled) {
            if (!colorMode.isMonet) colorMode.toMonetMode() else currentThemeMode
        } else {
            if (colorMode.isMonet) colorMode.toNonMonetMode() else currentThemeMode
        }
        repo.miuixMonet = enabled
        repo.themeMode = newThemeMode
        _uiState.update { it.copy(miuixMonet = enabled, themeMode = newThemeMode) }
    }

    fun setKeyColor(color: Int) {
        repo.keyColor = color
        _uiState.update { it.copy(keyColor = color) }
    }

    fun setColorStyle(style: String) {
        repo.colorStyle = style
        _uiState.update { it.copy(colorStyle = style) }
    }

    fun setColorSpec(spec: String) {
        repo.colorSpec = spec
        _uiState.update { it.copy(colorSpec = spec) }
    }

    fun setEnablePredictiveBack(enabled: Boolean) {
        repo.enablePredictiveBack = enabled
        _uiState.update { it.copy(enablePredictiveBack = enabled) }
    }

    fun setEnableSwipeDismiss(enabled: Boolean) {
        repo.enableSwipeDismiss = enabled
        _uiState.update { it.copy(enableSwipeDismiss = enabled) }
    }

    fun setPagerInterceptionMode(mode: Int) {
        repo.pagerInterceptionMode = mode
        _uiState.update { it.copy(pagerInterceptionMode = mode.coerceIn(0, 2)) }
    }

    fun setEnableBlur(enabled: Boolean) {
        repo.enableBlur = enabled
        _uiState.update { it.copy(enableBlur = enabled) }
    }

    fun setEnableFloatingBottomBar(enabled: Boolean) {
        repo.enableFloatingBottomBar = enabled
        _uiState.update { it.copy(enableFloatingBottomBar = enabled) }
    }

    fun setEnableFloatingBottomBarBlur(enabled: Boolean) {
        repo.enableFloatingBottomBarBlur = enabled
        _uiState.update { it.copy(enableFloatingBottomBarBlur = enabled) }
    }

    fun setEnableNavigationBadge(enabled: Boolean) {
        repo.enableNavigationBadge = enabled
        _uiState.update { it.copy(enableNavigationBadge = enabled) }
    }

    fun setPageScale(scale: Float) {
        repo.pageScale = scale
        _uiState.update { it.copy(pageScale = scale) }
    }

    fun setModuleDescriptionMaxLines(lines: Int) {
        repo.moduleDescriptionMaxLines = lines
        _uiState.update { it.copy(moduleDescriptionMaxLines = lines) }
    }

    fun setEnableWebDebugging(enabled: Boolean) {
        repo.enableWebDebugging = enabled
        _uiState.update { it.copy(enableWebDebugging = enabled) }
    }

    fun setSuCompatMode(mode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (mode) {
                0 -> if (repo.setSuEnabled(true)) {
                    repo.execKsudFeatureSave()
                    repo.setSuCompatModePref(0)
                    _uiState.update { it.copy(suCompatMode = 0, isSuEnabled = true) }
                }

                1 -> if (repo.setSuEnabled(true)) {
                    repo.execKsudFeatureSave()
                    if (repo.setSuEnabled(false)) {
                        // "Disable until reboot" implies it should be enabled on next boot.
                        // We set the preference to 0 (Enabled) to match the persistent state.
                        repo.setSuCompatModePref(0)
                        _uiState.update { it.copy(suCompatMode = 1, isSuEnabled = false) }
                    }
                }

                2 -> if (repo.setSuEnabled(false)) {
                    repo.execKsudFeatureSave()
                    repo.setSuCompatModePref(2)
                    _uiState.update { it.copy(suCompatMode = 2, isSuEnabled = false) }
                }
            }
        }
    }

    fun setKernelUmountEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setKernelUmountEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isKernelUmountEnabled = enabled) }
            }
        }
    }

    fun setSelinuxHideEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val status = repo.setSelinuxHideEnabled(enabled)
            repo.execKsudFeatureSave()
            _uiState.update { it.copy(isSelinuxHideEnabled = enabled) }
            when (status) {
                0 -> {}
                -OsConstants.EAGAIN -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ksuApp, R.string.settings_selinux_hide_reboot_required,
                            Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ksuApp, ksuApp.getString(R.string.settings_selinux_hide_failed, status),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun setAutoJailbreak(enabled: Boolean) {
        repo.autoJailbreak = enabled
        _uiState.update { it.copy(autoJailbreak = enabled) }
    }

    fun setUseSoftReboot(enabled: Boolean) {
        repo.useSoftReboot = enabled
        _uiState.update { it.copy(useSoftReboot = enabled) }
    }

    fun setSulogEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setSulogEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isSulogEnabled = enabled) }
            }
        }
    }

    fun setShellSuAdbOnlyEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setShellSuAdbOnlyEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isShellSuAdbOnlyEnabled = enabled) }
            }
        }
    }

    fun setAdbRootEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setAdbRootEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isAdbRootEnabled = enabled) }
            }
        }
    }

    fun setDefaultUmountModules(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setDefaultUmountModules(enabled)) {
                _uiState.update { it.copy(isDefaultUmountModules = enabled) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Theme appearance forwarders: write the repository, then mirror into
    // the UI state. Package apply/trial flows (P1) reuse the same setters.
    // ------------------------------------------------------------------

    fun setCustomHomeTitle(value: String) {
        repo.customHomeTitle = value
        _uiState.update { it.copy(customHomeTitle = value.trim()) }
    }

    fun setAppLanguage(tag: String) {
        repo.appLanguage = tag
        _uiState.update { it.copy(appLanguage = tag) }
    }

    fun setCustomTexts(value: Map<String, String>) {
        repo.customTexts = value
        _uiState.update { it.copy(customTexts = value) }
    }

    fun setCustomText(key: String, value: String) {
        setCustomTexts(repo.customTexts + (key to value))
    }

    fun setCardBackgroundAlphaPercent(value: Int) {
        val coerced = value.coerceIn(10, 91)
        repo.cardBackgroundAlphaPercent = coerced
        _uiState.update { it.copy(cardBackgroundAlphaPercent = coerced) }
    }

    fun setCustomBackgroundBlurPercent(value: Int) {
        val coerced = value.coerceIn(0, 100)
        repo.customBackgroundBlurPercent = coerced
        _uiState.update { it.copy(customBackgroundBlurPercent = coerced) }
    }

    fun setCustomBackgroundScrimPercent(value: Int) {
        val coerced = value.coerceIn(-100, 100)
        repo.customBackgroundScrimPercent = coerced
        _uiState.update { it.copy(customBackgroundScrimPercent = coerced) }
    }

    fun setUseSystemWallpaper(enabled: Boolean) {
        repo.useSystemWallpaper = enabled
        _uiState.update { it.copy(useSystemWallpaper = enabled) }
        if (enabled) captureSystemWallpaper()
    }

    /**
     * Copies the launcher wallpaper through the root shell. Android 13+ hides it from
     * WallpaperManager, so this is the only reliable path and it has to run off the main thread.
     */
    private fun captureSystemWallpaper() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSystemWallpaperBusy = true) }
            val result = ImageStorage.captureSystemWallpaper(ksuApp, force = true)
            val success = result.path != null && _uiState.value.useSystemWallpaper
            if (!success) {
                repo.useSystemWallpaper = false
            }
            _uiState.update {
                it.copy(
                    isSystemWallpaperBusy = false,
                    systemWallpaperPath = result.path.orEmpty(),
                    useSystemWallpaper = success,
                )
            }
            if (!success) toast(R.string.settings_use_system_wallpaper_failed)
        }
    }

    fun setExtractColorFromBackground(enabled: Boolean) {
        repo.extractColorFromBackground = enabled
        _uiState.update { it.copy(extractColorFromBackground = enabled) }
    }

    fun setShowFullStatus(enabled: Boolean) {
        repo.showFullStatus = enabled
        _uiState.update { it.copy(showFullStatus = enabled) }
    }

    fun setHideHomeWarnings(enabled: Boolean) {
        repo.hideHomeWarnings = enabled
        _uiState.update { it.copy(hideHomeWarnings = enabled) }
    }

    fun setHomeLayout(value: String) {
        repo.homeLayout = value
        _uiState.update { it.copy(homeLayout = value) }
    }

    fun setHomeStatusAlignment(value: String) {
        repo.homeStatusAlignment = value
        _uiState.update { it.copy(homeStatusAlignment = value) }
    }

    fun setHomeStatusScale(value: Float) {
        val coerced = value.coerceIn(0.6f, 2f)
        repo.homeStatusScale = coerced
        _uiState.update { it.copy(homeStatusScale = coerced) }
    }

    fun setHomePillScale(value: Float) {
        val coerced = value.coerceIn(0.6f, 2f)
        repo.homePillScale = coerced
        _uiState.update { it.copy(homePillScale = coerced) }
    }

    fun setHomeStatusWidthFraction(value: Float) {
        val coerced = value.coerceIn(0.4f, 1f)
        repo.homeStatusWidthFraction = coerced
        _uiState.update { it.copy(homeStatusWidthFraction = coerced) }
    }

    fun setHideHomeCardStatusText(enabled: Boolean) {
        repo.hideHomeCardStatusText = enabled
        _uiState.update { it.copy(hideHomeCardStatusText = enabled) }
    }

    fun setHideHomeCardDecoration(enabled: Boolean) {
        repo.hideHomeCardDecoration = enabled
        _uiState.update { it.copy(hideHomeCardDecoration = enabled) }
    }

    fun setHomeCardImageStyle(value: String) {
        repo.homeCardImageStyle = value
        _uiState.update { it.copy(homeCardImageStyle = value) }
    }

    fun setShowHomeCardImageWithoutBackground(enabled: Boolean) {
        repo.showHomeCardImageWithoutBackground = enabled
        _uiState.update { it.copy(showHomeCardImageWithoutBackground = enabled) }
    }

    fun setHomeCardImagePath(path: String) {
        repo.homeCardImagePath = path
        _uiState.update { it.copy(homeCardImagePath = path) }
    }

    fun setAdaptiveAppliedMode(mode: Int) {
        repo.adaptiveAppliedMode = mode
        _uiState.update { it.copy(adaptiveAppliedMode = mode) }
    }

    fun setCustomAppearanceJson(json: String) {
        repo.customAppearanceJson = json
        _uiState.update { it.copy(customAppearanceJson = json) }
    }

    fun setAppIconId(iconId: String) {
        repo.appIconId = iconId
        _uiState.update { it.copy(appIconId = iconId) }
    }

    fun setAppIconDynamicColor(enabled: Boolean) {
        repo.appIconDynamicColor = enabled
        _uiState.update { it.copy(appIconDynamicColor = enabled) }
    }

    fun setEnableProgressiveBlur(enabled: Boolean) {
        repo.enableProgressiveBlur = enabled
        _uiState.update { it.copy(enableProgressiveBlur = enabled) }
    }

    fun setBottomBarIcon(slot: String, path: String) {
        val updated = repo.bottomBarIcons + (slot to path)
        repo.bottomBarIcons = updated
        _uiState.update { it.copy(bottomBarIcons = updated) }
    }

    fun clearBottomBarIcon(slot: String) {
        val updated = repo.bottomBarIcons - slot
        repo.bottomBarIcons = updated
        _uiState.update { it.copy(bottomBarIcons = updated) }
    }

    fun addHomeCardSticker(sticker: HomeCardSticker) {
        if (repo.homeCardStickers.size >= HomeCardSticker.MAX_COUNT) return
        val updated = repo.homeCardStickers + sticker
        repo.homeCardStickers = updated
        _uiState.update { it.copy(homeCardStickers = updated) }
    }

    fun updateHomeCardSticker(index: Int, transform: HomeCardStickerTransform) {
        val current = repo.homeCardStickers.toMutableList()
        if (index !in current.indices) return
        current[index] = current[index].copy(transform = transform.coerced())
        repo.homeCardStickers = current
        _uiState.update { it.copy(homeCardStickers = current) }
    }

    fun removeHomeCardSticker(index: Int) {
        val current = repo.homeCardStickers.toMutableList()
        if (index !in current.indices) return
        current.removeAt(index)
        repo.homeCardStickers = current
        _uiState.update { it.copy(homeCardStickers = current) }
    }

    fun setBackgroundAppearanceExpanded(expanded: Boolean) {
        repo.backgroundAppearanceExpanded = expanded
        _uiState.update { it.copy(backgroundAppearanceExpanded = expanded) }
    }

    fun setBackgroundSectionExpanded(expanded: Boolean) {
        repo.backgroundSectionExpanded = expanded
        _uiState.update { it.copy(backgroundSectionExpanded = expanded) }
    }

    fun setThemePackageSectionExpanded(expanded: Boolean) {
        repo.themePackageSectionExpanded = expanded
        _uiState.update { it.copy(themePackageSectionExpanded = expanded) }
    }

    fun setAppearanceSectionExpanded(expanded: Boolean) {
        repo.appearanceSectionExpanded = expanded
        _uiState.update { it.copy(appearanceSectionExpanded = expanded) }
    }

    fun setHomeCardSectionExpanded(expanded: Boolean) {
        repo.homeCardSectionExpanded = expanded
        _uiState.update { it.copy(homeCardSectionExpanded = expanded) }
    }

    fun setColorsSectionExpanded(expanded: Boolean) {
        repo.colorsSectionExpanded = expanded
        _uiState.update { it.copy(colorsSectionExpanded = expanded) }
    }

    fun setBottomBarSectionExpanded(expanded: Boolean) {
        repo.bottomBarSectionExpanded = expanded
        _uiState.update { it.copy(bottomBarSectionExpanded = expanded) }
    }

    fun setInteractionSectionExpanded(expanded: Boolean) {
        repo.interactionSectionExpanded = expanded
        _uiState.update { it.copy(interactionSectionExpanded = expanded) }
    }

    fun setDetailsSectionExpanded(expanded: Boolean) {
        repo.detailsSectionExpanded = expanded
        _uiState.update { it.copy(detailsSectionExpanded = expanded) }
    }

    // ------------------------------------------------------------------
    // Theme packages: import, apply, trial and export.
    // The manager owns rollback and image promotion; the view model only
    // mirrors the resulting state and reports success/failure.
    // ------------------------------------------------------------------

    fun importTheme(uri: Uri) {
        viewModelScope.launch {
            packages.importTheme(uri)
                .onSuccess { imported ->
                    _uiState.update {
                        it.copy(
                            pendingImportTheme = imported,
                            showThemeImportDialog = true,
                        )
                    }
                }
                .onFailure { toast(R.string.theme_import_failed) }
        }
    }

    fun importThemeFile(file: File) {
        viewModelScope.launch {
            packages.importThemeFile(file, deleteSource = true)
                .onSuccess { imported ->
                    _uiState.update {
                        it.copy(
                            pendingImportTheme = imported,
                            showThemeImportDialog = true,
                        )
                    }
                }
                .onFailure { toast(R.string.theme_import_failed) }
        }
    }

    fun dismissImportThemeDialog() {
        _uiState.value.pendingImportTheme?.let(packages::discard)
        _uiState.update {
            it.copy(pendingImportTheme = null, showThemeImportDialog = false)
        }
    }

    fun confirmImportTheme(options: ThemePackageOptions) {
        val pending = _uiState.value.pendingImportTheme ?: return
        viewModelScope.launch {
            val result = packages.apply(pending, options, clearFirst = true)
            _uiState.update {
                it.copy(pendingImportTheme = null, showThemeImportDialog = false)
            }
            result
                .onSuccess {
                    ThemeTrialManager.clear(ksuApp)
                    toast(R.string.theme_import_success)
                    refresh()
                }
                .onFailure { toast(R.string.theme_import_failed_restored, Toast.LENGTH_LONG) }
        }
    }

    fun startThemeTrial(packageFile: File, taskId: Int? = null) {
        viewModelScope.launch {
            packages.startTrial(packageFile, taskId)
                .onSuccess {
                    toast(R.string.theme_store_trial_started, Toast.LENGTH_LONG)
                    refresh()
                }
                .onFailure { toast(R.string.theme_store_trial_failed, Toast.LENGTH_LONG) }
        }
    }

    fun restoreThemeTrial(taskId: Int? = null) {
        viewModelScope.launch {
            if (packages.restoreTrialIfNeeded(taskId)) refresh()
        }
    }

    fun exportTheme(uri: Uri, options: ThemePackageOptions) {
        viewModelScope.launch {
            packages.exportTheme(uri, options)
                .onSuccess { toast(R.string.theme_export_success) }
                .onFailure { toast(R.string.theme_export_failed) }
        }
    }

    suspend fun createCurrentThemePackage(
        options: ThemePackageOptions = ThemePackageOptions(),
        preferDraft: Boolean = true,
    ): Result<File> = packages.createCurrentThemePackage(options, preferDraft)

    fun applyCustomAppearance(data: AppearanceOverlayData) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { AppearanceOverlayStore.save(repo, data) }
            val saved = AppearanceOverlayStore.data(repo)
            _uiState.update {
                it.copy(
                    customAppearanceJson = repo.customAppearanceJson,
                    customColorsPresent = saved?.colors != null,
                    imageEffectsPresent = saved?.image != null,
                    appearance = AppearanceOverlayStore.load(repo),
                    appearanceData = saved,
                )
            }
        }
    }

    fun resetCustomAppearance() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { AppearanceOverlayStore.clear(repo) }
            _uiState.update {
                it.copy(
                    customAppearanceJson = "",
                    customColorsPresent = false,
                    imageEffectsPresent = false,
                    appearance = null,
                    appearanceData = null,
                )
            }
        }
    }

    fun restoreDefaultTheme() {
        viewModelScope.launch {
            packages.restoreDefault()
                .onSuccess { refresh() }
                .onFailure { toast(R.string.theme_import_failed) }
        }
    }

    private fun toast(resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(ksuApp, resId, duration).show()
        }
    }
}
