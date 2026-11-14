package me.weishu.kernelsu.ui.theme.themepackage

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.screen.settings.ThemePackageOptions
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.CustomTextGroup
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.customTextsInGroups
import me.weishu.kernelsu.ui.util.AdaptiveThemeState
import me.weishu.kernelsu.ui.util.AdaptiveThemeStore
import me.weishu.kernelsu.ui.util.AdaptiveThemeVariantState
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.AppearanceOverlayStore
import me.weishu.kernelsu.ui.util.BottomBarIconSlot
import me.weishu.kernelsu.ui.util.BuiltinAppIcons
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.ImportedTheme
import me.weishu.kernelsu.ui.util.ImportedThemeVariant
import me.weishu.kernelsu.ui.util.ThemePackageAdaptiveExport
import me.weishu.kernelsu.ui.util.ThemePackageData
import me.weishu.kernelsu.ui.util.ThemePackageStorage
import me.weishu.kernelsu.ui.util.ThemePackageVariantExport
import me.weishu.kernelsu.ui.util.ThemeTrialManager

/**
 * Aggregates the theme package lifecycle: import, permanent apply with rollback, trial,
 * default restore and export.
 *
 * The repository is the only persistent store. The manager never holds UI state; callers
 * refresh their snapshot after an operation completes.
 */
class ThemePackageManager(
    private val context: Context = ksuApp,
    private val repo: SettingsRepository = SettingsRepositoryImpl(),
) {

    // ------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------

    suspend fun importTheme(uri: Uri): Result<ImportedTheme> =
        ThemePackageStorage.import(context, uri)

    /**
     * File-based import used by the store cache and by `ACTION_VIEW` intents.
     * [deleteSource] removes the received copy after the package was parsed.
     */
    suspend fun importThemeFile(file: File, deleteSource: Boolean = false): Result<ImportedTheme> =
        ThemePackageStorage.importFromFile(context, file).also {
            if (deleteSource) runCatching { file.delete() }
        }

    fun discard(imported: ImportedTheme) {
        imported.temporaryFiles().forEach { path ->
            runCatching { File(path).delete() }
        }
    }

    // ------------------------------------------------------------------
    // Apply
    // ------------------------------------------------------------------

    /**
     * Applies a parsed package.
     *
     * [clearFirst] resets the groups the package takes part in before writing, so a package
     * without a section cannot leave the previous theme's data behind. When [rollbackOnFailure]
     * is set, the current appearance is exported to a temporary package first and restored if
     * the apply step throws.
     */
    suspend fun apply(
        imported: ImportedTheme,
        options: ThemePackageOptions = ThemePackageOptions(),
        clearFirst: Boolean = true,
        rollbackOnFailure: Boolean = clearFirst,
    ): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val rollback = if (rollbackOnFailure) {
            runCatching { createCurrentThemePackage(ThemePackageOptions(), preferDraft = false).getOrThrow() }
                .getOrNull()
        } else {
            null
        }
        runCatching {
            applyInternal(imported, options, clearFirst)
        }.onFailure {
            AdaptiveThemeStore.clear(repo)
            if (imported.isAdaptive) {
                runCatching { ImageStorage.clearAdaptiveImages(context) }
            }
            discard(imported)
            if (rollback != null) {
                runCatching {
                    val restored = ThemePackageStorage.importFromFile(context, rollback).getOrThrow()
                    applyInternal(restored, ThemePackageOptions(), clearFirst = true)
                }
            }
        }.also {
            rollback?.let { file -> runCatching { file.delete() } }
        }
    }

    private suspend fun applyInternal(
        imported: ImportedTheme,
        options: ThemePackageOptions,
        clearFirst: Boolean,
    ) {
        if (clearFirst) resetAppearanceForImport(options, imported)

        val adaptiveState = if (imported.isAdaptive) {
            buildAdaptiveState(imported, options)
        } else {
            null
        }
        if (adaptiveState != null) {
            AdaptiveThemeStore.save(repo, adaptiveState)
        } else {
            AdaptiveThemeStore.clear(repo)
        }

        // Adaptive packages keep their appearance on the two variants; the legacy sections are
        // masked so they cannot overwrite what AdaptiveThemeStore applies below.
        val data = if (imported.isAdaptive) imported.data.masked() else imported.data

        if (options.colors) {
            data.themeMode?.takeIf { it in 0..6 }?.let { repo.themeMode = it }
            data.miuixMonet?.let { repo.miuixMonet = it }
            data.keyColor?.let { repo.keyColor = it }
            data.colorStyle
                ?.takeIf { runCatching { PaletteStyle.valueOf(it) }.isSuccess }
                ?.let { repo.colorStyle = it }
            data.colorSpec
                ?.takeIf { runCatching { ColorSpec.SpecVersion.valueOf(it) }.isSuccess }
                ?.let { repo.colorSpec = it }
        }

        if (options.backgroundAppearance) {
            data.cardBackgroundAlphaPercent?.let {
                repo.cardBackgroundAlphaPercent = it
                    .coerceIn(ImageStorage.CARD_ALPHA_MIN, ImageStorage.CARD_ALPHA_MAX)
            }
            data.customBackgroundBlurPercent?.let {
                repo.customBackgroundBlurPercent = it.coerceIn(
                    ImageStorage.BACKGROUND_BLUR_PERCENT_MIN,
                    ImageStorage.BACKGROUND_BLUR_PERCENT_MAX,
                )
            }
            data.customBackgroundScrimPercent?.let {
                repo.customBackgroundScrimPercent = it.coerceIn(
                    ImageStorage.BACKGROUND_SCRIM_MIN,
                    ImageStorage.BACKGROUND_SCRIM_MAX,
                )
            }
        }

        if (options.homeCardDisplay) {
            data.hideHomeCardDecoration?.let { repo.hideHomeCardDecoration = it }
            data.hideHomeCardStatusText?.let { repo.hideHomeCardStatusText = it }
            data.homeLayout?.let { repo.homeLayout = HomeLayout.fromValue(it).value }
            data.homeStatusAlignment?.let {
                repo.homeStatusAlignment = HomeStatusAlignment.fromValue(it).value
            }
            data.homeStatusScale?.let { repo.homeStatusScale = HomeStatusStyle.coerceScale(it) }
            data.homePillScale?.let { repo.homePillScale = HomeStatusStyle.coercePillScale(it) }
            data.homeStatusWidthFraction?.let {
                repo.homeStatusWidthFraction = HomeStatusStyle.coerceWidth(it)
            }
            data.homeCardImageStyle?.let {
                repo.homeCardImageStyle = HomeCardImageStyle.fromValue(it).value
            }
            data.showHomeCardImageWithoutBackground?.let {
                repo.showHomeCardImageWithoutBackground = it
            }
        }

        if (options.interfaceMode) {
            data.uiMode
                ?.takeIf { it == "miuix" || it == "material" }
                ?.let { repo.uiMode = it }
        }

        if (options.interfaceSwitches) {
            data.enableBlur?.let { repo.enableBlur = it }
            data.enableProgressiveBlur?.let { repo.enableProgressiveBlur = it }
            data.enableFloatingBottomBar?.let { repo.enableFloatingBottomBar = it }
            data.enableFloatingBottomBarBlur?.let { repo.enableFloatingBottomBarBlur = it }
            data.enableNavigationBadge?.let { repo.enableNavigationBadge = it }
        }

        if (options.interactionBehavior) {
            data.enablePredictiveBack?.let { repo.enablePredictiveBack = it }
            data.enableSwipeDismiss?.let { repo.enableSwipeDismiss = it }
            data.pagerInterceptionMode?.let { repo.pagerInterceptionMode = it.coerceIn(0, 2) }
            data.pageScale?.let { repo.pageScale = it.coerceIn(0.8f, 1.1f) }
        }

        if (options.moduleDisplay) {
            data.moduleSortEnabledFirst?.let { repo.moduleSortEnabledFirst = it }
            data.moduleSortActionFirst?.let { repo.moduleSortActionFirst = it }
            data.moduleSortWebUiFirst?.let { repo.moduleSortWebUiFirst = it }
            data.moduleSortZygiskFirst?.let { repo.moduleSortZygiskFirst = it }
            data.moduleSortMetamoduleFirst?.let { repo.moduleSortMetamoduleFirst = it }
            data.moduleSortNameOrder
                ?.takeIf { it.isNotBlank() }
                ?.let { repo.moduleSortNameOrder = it }
            data.moduleSortManual?.let { repo.moduleSortManual = it }
            data.moduleDescriptionMaxLines?.let {
                repo.moduleDescriptionMaxLines = it.coerceIn(1, 5)
            }
        }

        // --- images -----------------------------------------------------
        if (options.backgroundImage) {
            if (imported.backgroundPath != null) {
                val path = ImageStorage.replace(context, File(imported.backgroundPath))
                    ?: error("Failed to save background")
                repo.useSystemWallpaper = false
                repo.customBackgroundUri = path
                repo.customBackgroundUpdatedAt = System.currentTimeMillis()
            } else if (data.hasBackground == false) {
                ImageStorage.clear(context)
                repo.customBackgroundUri = ""
                repo.useSystemWallpaper = false
                repo.customBackgroundUpdatedAt = System.currentTimeMillis()
            }
        } else {
            imported.backgroundPath?.let { runCatching { File(it).delete() } }
        }

        if (options.homeCardImage) {
            if (imported.cardBackgroundPath != null) {
                repo.homeCardImagePath = ImageStorage.replaceHomeCardImage(
                    context,
                    File(imported.cardBackgroundPath),
                ) ?: error("Failed to save home card image")
            } else if (data.hasCardBackground == false) {
                ImageStorage.clearHomeCardImage(context)
                repo.homeCardImagePath = ""
            }
        } else {
            imported.cardBackgroundPath?.let { runCatching { File(it).delete() } }
        }

        if (options.stickers) {
            if (imported.stickerPaths.isNotEmpty()) {
                val transforms = data.homeCardStickerTransforms.orEmpty()
                val importedStickers = imported.stickerPaths.mapIndexedNotNull { index, path ->
                    val saved = ImageStorage.importHomeCardSticker(context, File(path))
                        ?: return@mapIndexedNotNull null
                    HomeCardSticker(
                        path = saved,
                        transform = transforms.getOrNull(index)?.coerced()
                            ?: HomeCardStickerTransform(),
                    )
                }.take(HomeCardSticker.MAX_COUNT)
                if (importedStickers.isNotEmpty()) {
                    repo.homeCardStickers.forEach {
                        ImageStorage.deleteHomeCardSticker(context, it.path)
                    }
                    repo.homeCardStickers = importedStickers
                }
            } else if (data.homeCardStickerTransforms != null) {
                // A package with an empty list explicitly clears the existing stickers.
                repo.homeCardStickers.forEach {
                    ImageStorage.deleteHomeCardSticker(context, it.path)
                }
                repo.homeCardStickers = emptyList()
            }
        } else {
            imported.stickerPaths.forEach { runCatching { File(it).delete() } }
        }

        if (options.bottomBarIcons) {
            // The field doubles as the "this package owns the icons" marker: an empty list
            // explicitly clears the current icons, a missing field leaves them untouched.
            if (data.bottomBarIcons != null) {
                ImageStorage.clearBottomBarIcons(context)
                repo.bottomBarIcons = imported.bottomBarIconPaths.mapNotNull { (slotKey, path) ->
                    val slot = BottomBarIconSlot.fromKey(slotKey) ?: return@mapNotNull null
                    ImageStorage.importBottomBarIcon(context, slot, File(path))
                        ?.let { slotKey to it }
                }.toMap()
            }
        } else {
            imported.bottomBarIconPaths.values.forEach { runCatching { File(it).delete() } }
        }

        // Launcher alias switching follows the persisted id so a package icon is visible
        // immediately instead of only after the next launch.
        if (options.appIcon) {
            val targetIconId = data.appIconId?.takeIf { id ->
                BuiltinAppIcons.all.any { it.id == id }
            }
            if (targetIconId != null) {
                repo.appIconId = targetIconId
                BuiltinAppIcons.apply(
                    context = context,
                    icon = BuiltinAppIcons.find(targetIconId),
                    dynamicColor = repo.appIconDynamicColor,
                )
            }
        }

        if (options.customTexts) {
            val selectedGroups = options.customTextGroups
            // Only the selected categories are replaced; the others keep their current text.
            val textsMap = repo.customTexts.toMutableMap()
            textsMap.keys.toList().forEach { key ->
                val group = CustomTextKey.fromKey(key)?.group
                if (group != null && group in selectedGroups) textsMap.remove(key)
            }
            (data.customTexts ?: emptyMap()).forEach { (key, value) ->
                val group = CustomTextKey.fromKey(key)?.group
                if (group != null && group in selectedGroups && value.isNotBlank()) {
                    textsMap[key] = rebrandThemeText(value)
                }
            }
            if (CustomTextGroup.HOME in selectedGroups) {
                val homeTitle = (data.customHomeTitle ?: textsMap[CustomTextKey.HOME_TITLE.key])
                    ?.let(::rebrandThemeText)
                if (!homeTitle.isNullOrBlank()) {
                    textsMap[CustomTextKey.HOME_TITLE.key] = homeTitle
                    repo.customHomeTitle = homeTitle
                } else {
                    repo.customHomeTitle = ""
                }
            }
            repo.customTexts = textsMap
        }

        if (options.customColors || options.imageEffects) {
            // Selected sections are replaced as a whole; unselected sections keep the user's
            // current overlay. A package without the selected section clears it.
            val merged = AppearanceOverlayStore.merge(
                current = AppearanceOverlayStore.data(repo),
                incoming = data.appearance,
                applyColors = options.customColors,
                applyImageEffects = options.imageEffects,
            )
            if (merged == null) {
                AppearanceOverlayStore.clear(repo)
            } else {
                AppearanceOverlayStore.save(repo, merged)
            }
        }

        if (adaptiveState != null) {
            val dark = isEffectiveDarkMode()
            AdaptiveThemeStore.apply(repo, adaptiveState.variant(dark))
            repo.adaptiveAppliedMode = if (dark) 1 else 0
        }

        discard(imported)
    }

    /**
     * Clears every group selected by [options] before the package data is written. Groups the
     * package does not carry lose the previous values; groups the user unticked keep them.
     */
    private suspend fun resetAppearanceForImport(
        options: ThemePackageOptions,
        imported: ImportedTheme,
    ) {
        if (options.colors) {
            deactivateAdaptiveTheme()
            repo.themeMode = 0
            repo.miuixMonet = false
            repo.keyColor = 0
            repo.extractColorFromBackground = false
            repo.colorStyle = PaletteStyle.TonalSpot.name
            repo.colorSpec = ColorSpec.SpecVersion.SPEC_2025.name
        }
        val backgroundImageOptionAvailable =
            imported.backgroundPath != null ||
                imported.light?.backgroundPath != null ||
                imported.dark?.backgroundPath != null ||
                (!imported.isAdaptive && imported.data.hasBackground == false)
        if (options.backgroundImage || !backgroundImageOptionAvailable) {
            ImageStorage.clear(context)
            runCatching { ImageStorage.systemWallpaperFile(context).delete() }
            repo.customBackgroundUri = ""
            repo.customBackgroundUpdatedAt = 0L
            repo.useSystemWallpaper = false
        }
        if (options.backgroundAppearance) {
            val defaultBlur = ImageStorage.defaultBackgroundBlurPercent(context)
            repo.cardBackgroundAlphaPercent = ImageStorage.DEFAULT_CARD_ALPHA
            repo.customBackgroundBlurPercent = defaultBlur
            repo.customBackgroundScrimPercent = ImageStorage.DEFAULT_BACKGROUND_SCRIM
        }
        val homeCardImageOptionAvailable =
            imported.cardBackgroundPath != null ||
                imported.light?.cardBackgroundPath != null ||
                imported.dark?.cardBackgroundPath != null ||
                (!imported.isAdaptive && imported.data.hasCardBackground == false)
        if (options.homeCardImage || !homeCardImageOptionAvailable) {
            ImageStorage.clearHomeCardImage(context)
            repo.homeCardImagePath = ""
        }
        val stickersOptionAvailable =
            imported.stickerPaths.isNotEmpty() ||
                imported.light?.stickerPaths?.isNotEmpty() == true ||
                imported.dark?.stickerPaths?.isNotEmpty() == true ||
                (!imported.isAdaptive && imported.data.homeCardStickerTransforms != null)
        if (options.stickers || !stickersOptionAvailable) {
            ImageStorage.clearHomeCardSticker(context)
            repo.homeCardStickers = emptyList()
        }
        if (options.homeCardDisplay) {
            repo.hideHomeCardStatusText = false
            repo.hideHomeCardDecoration = false
            repo.homeLayout = HomeLayout.CLASSIC.value
            repo.homeStatusAlignment = HomeStatusAlignment.START.value
            repo.homeStatusScale = HomeStatusStyle.SCALE_DEFAULT
            repo.homePillScale = HomeStatusStyle.PILL_SCALE_DEFAULT
            repo.homeStatusWidthFraction = HomeStatusStyle.WIDTH_DEFAULT
            repo.homeCardImageStyle = HomeCardImageStyle.BANNER.value
            repo.showHomeCardImageWithoutBackground = false
        }
        if (options.interfaceSwitches) {
            repo.enableBlur = false
            repo.enableProgressiveBlur = false
            repo.enableFloatingBottomBar = false
            repo.enableFloatingBottomBarBlur = false
            repo.enableNavigationBadge = true
        }
        if (options.appIcon && repo.appIconId != BuiltinAppIcons.DEFAULT_ID) {
            repo.appIconId = BuiltinAppIcons.DEFAULT_ID
            BuiltinAppIcons.apply(
                context = context,
                icon = BuiltinAppIcons.find(BuiltinAppIcons.DEFAULT_ID),
                dynamicColor = repo.appIconDynamicColor,
            )
        }
        val packageHasBottomBarIcons =
            imported.data.bottomBarIcons != null || imported.bottomBarIconPaths.isNotEmpty()
        if (options.bottomBarIcons || !packageHasBottomBarIcons) {
            ImageStorage.clearBottomBarIcons(context)
            repo.bottomBarIcons = emptyMap()
        }
    }

    private fun ThemePackageData.masked(): ThemePackageData = copy(
        themeMode = null,
        miuixMonet = null,
        keyColor = null,
        colorStyle = null,
        colorSpec = null,
        cardBackgroundAlphaPercent = null,
        customBackgroundBlurPercent = null,
        customBackgroundScrimPercent = null,
        hasBackground = null,
        hasCardBackground = null,
        hideHomeCardStatusText = null,
        hideHomeCardDecoration = null,
        homeCardStickerTransforms = null,
        homeLayout = null,
        homeStatusAlignment = null,
        homeStatusScale = null,
        homePillScale = null,
        homeStatusWidthFraction = null,
        homeCardImageStyle = null,
        showHomeCardImageWithoutBackground = null,
        uiMode = null,
        enableFloatingBottomBar = null,
        enableFloatingBottomBarBlur = null,
        enableNavigationBadge = null,
        enablePredictiveBack = null,
        enableSwipeDismiss = null,
        enableBlur = null,
        enableProgressiveBlur = null,
        pagerInterceptionMode = null,
        pageScale = null,
        moduleDescriptionMaxLines = null,
        moduleSortEnabledFirst = null,
        moduleSortActionFirst = null,
        moduleSortWebUiFirst = null,
        moduleSortZygiskFirst = null,
        moduleSortMetamoduleFirst = null,
        moduleSortNameOrder = null,
        moduleSortManual = null,
    )

    private suspend fun buildAdaptiveState(
        imported: ImportedTheme,
        options: ThemePackageOptions,
    ): AdaptiveThemeState {
        val light = imported.light ?: error("Light variant is missing")
        val dark = imported.dark ?: error("Dark variant is missing")
        return AdaptiveThemeState(
            light = buildVariantState(light, dark = false, options = options),
            dark = buildVariantState(dark, dark = true, options = options),
        )
    }

    private suspend fun buildVariantState(
        variant: ImportedThemeVariant,
        dark: Boolean,
        options: ThemePackageOptions,
    ): AdaptiveThemeVariantState {
        val backgroundPath = if (options.backgroundImage) {
            if (variant.backgroundPath != null) {
                requireNotNull(
                    ImageStorage.replaceAdaptiveBackground(
                        context,
                        File(variant.backgroundPath),
                        dark,
                    ),
                ) { "Failed to save adaptive background" }
            } else {
                ImageStorage.clearAdaptiveBackground(context, dark)
                ""
            }
        } else {
            ""
        }
        val cardBackgroundPath = if (options.homeCardImage) {
            if (variant.cardBackgroundPath != null) {
                requireNotNull(
                    ImageStorage.replaceAdaptiveHomeCardImage(
                        context,
                        File(variant.cardBackgroundPath),
                        dark,
                    ),
                ) { "Failed to save adaptive card background" }
            } else {
                ImageStorage.clearAdaptiveHomeCardImage(context, dark)
                ""
            }
        } else {
            ""
        }
        val stickerTransforms = variant.data.homeCardStickerTransforms
        val stickers = if (options.stickers) {
            ImageStorage.clearAdaptiveHomeCardStickers(context, dark)
            if (variant.stickerPaths.isEmpty()) {
                emptyList()
            } else {
                variant.stickerPaths.mapIndexedNotNull { index, path ->
                    val saved = ImageStorage.importAdaptiveHomeCardSticker(
                        context,
                        File(path),
                        dark,
                        index,
                    ) ?: return@mapIndexedNotNull null
                    HomeCardSticker(
                        path = saved,
                        transform = stickerTransforms?.getOrNull(index)?.coerced()
                            ?: HomeCardStickerTransform(),
                    )
                }.take(HomeCardSticker.MAX_COUNT)
            }
        } else {
            emptyList()
        }
        val stickerData = when {
            !options.stickers -> null
            stickerTransforms != null || variant.stickerPaths.isNotEmpty() ->
                stickers.map { it.transform }
            else -> null
        }

        return AdaptiveThemeVariantState(
            data = variant.data.copy(
                // A missing mode still binds to the system switch; only the monet flavour is kept.
                themeMode = if (options.colors) variant.data.themeMode ?: 0 else null,
                miuixMonet = if (options.colors) variant.data.miuixMonet else null,
                keyColor = if (options.colors) variant.data.keyColor else null,
                colorStyle = if (options.colors) variant.data.colorStyle else null,
                colorSpec = if (options.colors) variant.data.colorSpec else null,
                cardBackgroundAlphaPercent = if (options.backgroundAppearance) {
                    variant.data.cardBackgroundAlphaPercent
                } else {
                    null
                },
                customBackgroundBlurPercent = if (options.backgroundAppearance) {
                    variant.data.customBackgroundBlurPercent
                } else {
                    null
                },
                customBackgroundScrimPercent = if (options.backgroundAppearance) {
                    variant.data.customBackgroundScrimPercent
                } else {
                    null
                },
                hasBackground = if (options.backgroundImage) variant.data.hasBackground else null,
                hasCardBackground = if (options.homeCardImage) {
                    variant.data.hasCardBackground
                } else {
                    null
                },
                hideHomeCardStatusText = if (options.homeCardDisplay) {
                    variant.data.hideHomeCardStatusText
                } else {
                    null
                },
                hideHomeCardDecoration = if (options.homeCardDisplay) {
                    variant.data.hideHomeCardDecoration
                } else {
                    null
                },
                homeLayout = if (options.homeCardDisplay) variant.data.homeLayout else null,
                homeStatusAlignment = if (options.homeCardDisplay) {
                    variant.data.homeStatusAlignment
                } else {
                    null
                },
                homeStatusScale = if (options.homeCardDisplay) variant.data.homeStatusScale else null,
                homePillScale = if (options.homeCardDisplay) variant.data.homePillScale else null,
                homeStatusWidthFraction = if (options.homeCardDisplay) {
                    variant.data.homeStatusWidthFraction
                } else {
                    null
                },
                homeCardImageStyle = if (options.homeCardDisplay) {
                    variant.data.homeCardImageStyle
                } else {
                    null
                },
                showHomeCardImageWithoutBackground = if (options.homeCardDisplay) {
                    variant.data.showHomeCardImageWithoutBackground
                } else {
                    null
                },
                homeCardStickerTransforms = stickerData,
                uiMode = if (options.interfaceMode) variant.data.uiMode else null,
                enableBlur = if (options.interfaceSwitches) variant.data.enableBlur else null,
                enableProgressiveBlur = if (options.interfaceSwitches) {
                    variant.data.enableProgressiveBlur
                } else {
                    null
                },
                enableFloatingBottomBar = if (options.interfaceSwitches) {
                    variant.data.enableFloatingBottomBar
                } else {
                    null
                },
                enableFloatingBottomBarBlur = if (options.interfaceSwitches) {
                    variant.data.enableFloatingBottomBarBlur
                } else {
                    null
                },
                enableNavigationBadge = if (options.interfaceSwitches) {
                    variant.data.enableNavigationBadge
                } else {
                    null
                },
                enablePredictiveBack = if (options.interactionBehavior) {
                    variant.data.enablePredictiveBack
                } else {
                    null
                },
                enableSwipeDismiss = if (options.interactionBehavior) {
                    variant.data.enableSwipeDismiss
                } else {
                    null
                },
                pagerInterceptionMode = if (options.interactionBehavior) {
                    variant.data.pagerInterceptionMode
                } else {
                    null
                },
                pageScale = if (options.interactionBehavior) variant.data.pageScale else null,
                moduleDescriptionMaxLines = if (options.moduleDisplay) {
                    variant.data.moduleDescriptionMaxLines
                } else {
                    null
                },
            ),
            backgroundPath = backgroundPath,
            cardBackgroundPath = cardBackgroundPath,
            stickers = stickers,
        )
    }

    // ------------------------------------------------------------------
    // Trial
    // ------------------------------------------------------------------

    fun trialActive(): Boolean = ThemeTrialManager.isActive(context)

    fun trialNeedsRestore(taskId: Int? = null): Boolean =
        ThemeTrialManager.shouldRestore(context, taskId)

    /**
     * Starts a trial: the current appearance is packaged once, then the received package is
     * applied. The launcher icon is never switched for a temporary trial.
     */
    suspend fun startTrial(packageFile: File, taskId: Int?): Result<Unit> =
        withContext(NonCancellable + Dispatchers.IO) {
            runCatching {
                if (ThemeTrialManager.activeBackup(context) == null) {
                    val backup = createCurrentThemePackage(
                        ThemePackageOptions(),
                        preferDraft = false,
                    ).getOrThrow()
                    val target = ThemeTrialManager.backupFile(context)
                    target.parentFile?.mkdirs()
                    backup.copyTo(target, overwrite = true)
                    backup.delete()
                    ThemeTrialManager.markActive(context, target, taskId)
                }
                try {
                    val imported = ThemePackageStorage.importFromFile(context, packageFile)
                        .getOrThrow()
                    applyInternal(
                        imported,
                        ThemePackageOptions(appIcon = false),
                        clearFirst = true,
                    )
                } catch (error: Throwable) {
                    restoreTrialBackup()
                    throw error
                }
            }.also {
                runCatching { packageFile.delete() }
            }
        }

    /** Restores the pre-trial appearance when the trial process or task is gone. */
    suspend fun restoreTrialIfNeeded(taskId: Int? = null): Boolean {
        if (!trialNeedsRestore(taskId)) return false
        return withContext(NonCancellable + Dispatchers.IO) { restoreTrialBackup() }
    }

    suspend fun cancelTrial(): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        runCatching {
            if (!restoreTrialBackup()) error("No active theme trial")
        }
    }

    private suspend fun restoreTrialBackup(): Boolean {
        val backup = ThemeTrialManager.activeBackup(context) ?: return false
        val imported = ThemePackageStorage.importFromFile(context, backup).getOrNull()
            ?: return false
        return runCatching {
            applyInternal(imported, ThemePackageOptions(), clearFirst = true)
        }.onSuccess {
            ThemeTrialManager.clear(context)
        }.isSuccess
    }

    // ------------------------------------------------------------------
    // Restore defaults
    // ------------------------------------------------------------------

    /**
     * Clears every appearance group this manager owns. Navigation and module display settings
     * are user preferences rather than theme data, so they are left untouched.
     */
    suspend fun restoreDefault(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            deactivateAdaptiveTheme()
            ImageStorage.clearAdaptiveImages(context)
            ImageStorage.clear(context)
            ImageStorage.clearHomeCardImage(context)
            ImageStorage.clearHomeCardSticker(context)
            ImageStorage.clearBottomBarIcons(context)
            runCatching { ImageStorage.systemWallpaperFile(context).delete() }

            repo.themeMode = 0
            repo.miuixMonet = false
            repo.keyColor = 0
            repo.extractColorFromBackground = false
            repo.colorStyle = PaletteStyle.TonalSpot.name
            repo.colorSpec = ColorSpec.SpecVersion.SPEC_2025.name
            repo.customBackgroundUri = ""
            repo.customBackgroundUpdatedAt = 0L
            repo.useSystemWallpaper = false
            repo.cardBackgroundAlphaPercent = ImageStorage.DEFAULT_CARD_ALPHA
            repo.customBackgroundBlurPercent = ImageStorage.defaultBackgroundBlurPercent(context)
            repo.customBackgroundScrimPercent = ImageStorage.DEFAULT_BACKGROUND_SCRIM
            repo.homeCardImagePath = ""
            repo.homeCardStickers = emptyList()
            repo.bottomBarIcons = emptyMap()
            repo.hideHomeCardStatusText = false
            repo.hideHomeCardDecoration = false
            repo.homeLayout = HomeLayout.CLASSIC.value
            repo.homeStatusAlignment = HomeStatusAlignment.START.value
            repo.homeStatusScale = HomeStatusStyle.SCALE_DEFAULT
            repo.homePillScale = HomeStatusStyle.PILL_SCALE_DEFAULT
            repo.homeStatusWidthFraction = HomeStatusStyle.WIDTH_DEFAULT
            repo.homeCardImageStyle = HomeCardImageStyle.BANNER.value
            repo.showHomeCardImageWithoutBackground = false
            repo.customTexts = emptyMap()
            repo.customHomeTitle = ""
            repo.customAppearanceJson = ""
            repo.appIconId = "default"
            repo.appIconDynamicColor = false
            BuiltinAppIcons.applyIfNeeded(
                context,
                BuiltinAppIcons.find(BuiltinAppIcons.DEFAULT_ID),
                dynamicColor = false,
            )
            repo.adaptiveThemeJson = ""
            repo.adaptiveAppliedMode = -1
            repo.adaptiveDraftJson = ""
            AppearanceOverlayStore.clear(repo)
            ThemeTrialManager.clear(context)
        }
    }

    // ------------------------------------------------------------------
    // Export
    // ------------------------------------------------------------------

    suspend fun exportTheme(uri: Uri, options: ThemePackageOptions): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = currentThemePayload(options, preferDraft = true)
                ThemePackageStorage.export(
                    context = context,
                    uri = uri,
                    data = payload.data,
                    backgroundFile = payload.backgroundFile,
                    cardBackgroundFile = payload.cardBackgroundFile,
                    cardStickerFiles = payload.stickerFiles,
                    bottomBarIconFiles = payload.bottomBarIconFiles,
                    adaptive = payload.adaptive,
                ).getOrThrow()
            }
        }

    /** Builds the current appearance as a package in the cache directory. */
    suspend fun createCurrentThemePackage(
        options: ThemePackageOptions = ThemePackageOptions(),
        preferDraft: Boolean = false,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = currentThemePayload(options, preferDraft)
            val file = File(context.cacheDir, "theme_export_${System.nanoTime()}.sukisu")
            ThemePackageStorage.exportToFile(
                file = file,
                data = payload.data,
                backgroundFile = payload.backgroundFile,
                cardBackgroundFile = payload.cardBackgroundFile,
                cardStickerFiles = payload.stickerFiles,
                bottomBarIconFiles = payload.bottomBarIconFiles,
                adaptive = payload.adaptive,
            ).getOrThrow()
            file
        }
    }

    /** Export payload for the adaptive draft (or the active adaptive theme). */
    fun adaptiveExport(options: ThemePackageOptions): ThemePackageAdaptiveExport? {
        val draft = AdaptiveThemeStore.loadDraft(repo)?.takeIf { it.complete }
        val state = if (draft != null) {
            AdaptiveThemeState(draft.light!!, draft.dark!!)
        } else {
            AdaptiveThemeStore.load(repo) ?: return null
        }
        return ThemePackageAdaptiveExport(
            light = buildVariantExport(state.light, options),
            dark = buildVariantExport(state.dark, options),
        )
    }

    private data class CurrentThemePayload(
        val data: ThemePackageData,
        val backgroundFile: File?,
        val cardBackgroundFile: File?,
        val stickerFiles: List<File>,
        val bottomBarIconFiles: Map<String, File>,
        val adaptive: ThemePackageAdaptiveExport?,
    )

    private fun currentThemePayload(
        options: ThemePackageOptions,
        preferDraft: Boolean,
    ): CurrentThemePayload {
        val draft = if (preferDraft) {
            AdaptiveThemeStore.loadDraft(repo)?.takeIf { it.complete }
        } else {
            null
        }
        val activeAdaptive = AdaptiveThemeStore.load(repo)
        val adaptive = when {
            draft != null -> ThemePackageAdaptiveExport(
                light = buildVariantExport(draft.light!!, options),
                dark = buildVariantExport(draft.dark!!, options),
            )
            activeAdaptive != null -> ThemePackageAdaptiveExport(
                light = buildVariantExport(activeAdaptive.light, options),
                dark = buildVariantExport(activeAdaptive.dark, options),
            )
            else -> null
        }
        val backgroundFile = if (options.backgroundImage) {
            if (repo.useSystemWallpaper) {
                ImageStorage.systemWallpaperFile(context).takeIf { it.isFile }
            } else {
                repo.customBackgroundUri
                    .takeIf { it.isNotEmpty() }
                    ?.let { path -> File(path).takeIf { it.isFile } }
            }
        } else {
            null
        }
        val cardBackgroundFile = if (options.homeCardImage) {
            repo.homeCardImagePath
                .takeIf { it.isNotEmpty() }
                ?.let { path -> File(path).takeIf { it.isFile } }
        } else {
            null
        }
        val stickerEntries = if (options.stickers) {
            repo.homeCardStickers.filter { File(it.path).isFile }
        } else {
            emptyList()
        }
        val bottomBarIconFiles = if (options.bottomBarIcons) {
            repo.bottomBarIcons.mapNotNull { (slot, path) ->
                if (BottomBarIconSlot.fromKey(slot) == null) return@mapNotNull null
                File(path).takeIf { it.isFile }?.let { slot to it }
            }.toMap()
        } else {
            emptyMap()
        }
        val data = ThemePackageData(
            version = if (adaptive != null) {
                ThemePackageStorage.ADAPTIVE_VERSION
            } else {
                ThemePackageStorage.BASE_VERSION
            },
            themeMode = repo.themeMode.takeIf { options.colors },
            miuixMonet = repo.miuixMonet.takeIf { options.colors },
            keyColor = repo.keyColor.takeIf { options.colors },
            colorStyle = repo.colorStyle.takeIf { options.colors },
            colorSpec = repo.colorSpec.takeIf { options.colors },
            cardBackgroundAlphaPercent = repo.cardBackgroundAlphaPercent
                .takeIf { options.backgroundAppearance },
            customBackgroundBlurPercent = repo.customBackgroundBlurPercent
                .takeIf { options.backgroundAppearance },
            customBackgroundScrimPercent = repo.customBackgroundScrimPercent
                .takeIf { options.backgroundAppearance },
            hasBackground = if (options.backgroundImage) backgroundFile != null else null,
            hasCardBackground = if (options.homeCardImage) cardBackgroundFile != null else null,
            hideHomeCardStatusText = repo.hideHomeCardStatusText
                .takeIf { options.homeCardDisplay },
            hideHomeCardDecoration = repo.hideHomeCardDecoration
                .takeIf { options.homeCardDisplay },
            homeLayout = HomeLayout.fromValue(repo.homeLayout).value
                .takeIf { options.homeCardDisplay },
            homeStatusAlignment = HomeStatusAlignment.fromValue(repo.homeStatusAlignment).value
                .takeIf { options.homeCardDisplay },
            homeStatusScale = HomeStatusStyle.coerceScale(repo.homeStatusScale)
                .takeIf { options.homeCardDisplay },
            homePillScale = HomeStatusStyle.coercePillScale(repo.homePillScale)
                .takeIf { options.homeCardDisplay },
            homeStatusWidthFraction = HomeStatusStyle.coerceWidth(repo.homeStatusWidthFraction)
                .takeIf { options.homeCardDisplay },
            homeCardImageStyle = HomeCardImageStyle.fromValue(repo.homeCardImageStyle).value
                .takeIf { options.homeCardDisplay },
            showHomeCardImageWithoutBackground = repo.showHomeCardImageWithoutBackground
                .takeIf { options.homeCardDisplay },
            homeCardStickerTransforms = if (options.stickers) {
                stickerEntries.map { it.transform }
            } else {
                null
            },
            uiMode = repo.uiMode.takeIf { options.interfaceMode },
            appIconId = repo.appIconId.takeIf { options.appIcon },
            enableBlur = repo.enableBlur.takeIf { options.interfaceSwitches },
            enableProgressiveBlur = repo.enableProgressiveBlur.takeIf { options.interfaceSwitches },
            enableFloatingBottomBar = repo.enableFloatingBottomBar
                .takeIf { options.interfaceSwitches },
            enableFloatingBottomBarBlur = repo.enableFloatingBottomBarBlur
                .takeIf { options.interfaceSwitches },
            enableNavigationBadge = repo.enableNavigationBadge
                .takeIf { options.interfaceSwitches },
            enablePredictiveBack = repo.enablePredictiveBack.takeIf { options.interactionBehavior },
            enableSwipeDismiss = repo.enableSwipeDismiss.takeIf { options.interactionBehavior },
            pagerInterceptionMode = repo.pagerInterceptionMode
                .takeIf { options.interactionBehavior },
            pageScale = repo.pageScale.takeIf { options.interactionBehavior },
            moduleSortEnabledFirst = repo.moduleSortEnabledFirst.takeIf { options.moduleDisplay },
            moduleSortActionFirst = repo.moduleSortActionFirst.takeIf { options.moduleDisplay },
            moduleSortWebUiFirst = repo.moduleSortWebUiFirst.takeIf { options.moduleDisplay },
            moduleSortZygiskFirst = repo.moduleSortZygiskFirst.takeIf { options.moduleDisplay },
            moduleSortMetamoduleFirst = repo.moduleSortMetamoduleFirst
                .takeIf { options.moduleDisplay },
            moduleSortNameOrder = repo.moduleSortNameOrder.takeIf { options.moduleDisplay },
            moduleSortManual = repo.moduleSortManual.takeIf { options.moduleDisplay },
            moduleDescriptionMaxLines = repo.moduleDescriptionMaxLines
                .takeIf { options.moduleDisplay },
            customHomeTitle = if (
                options.customTexts && CustomTextGroup.HOME in options.customTextGroups
            ) {
                repo.customHomeTitle.takeIf { it.isNotBlank() }
            } else {
                null
            },
            customTexts = if (options.customTexts) {
                customTextsInGroups(repo.customTexts, options.customTextGroups)
                    .takeIf { it.isNotEmpty() }
            } else {
                null
            },
            bottomBarIcons = bottomBarIconFiles.keys.toList().takeIf { options.bottomBarIcons },
            appearance = currentAppearanceForExport(options),
        )
        return CurrentThemePayload(
            data = data,
            backgroundFile = backgroundFile,
            cardBackgroundFile = cardBackgroundFile,
            stickerFiles = stickerEntries.map { File(it.path) },
            bottomBarIconFiles = bottomBarIconFiles,
            adaptive = adaptive,
        )
    }

    private fun currentAppearanceForExport(options: ThemePackageOptions): AppearanceOverlayData? {
        val data = AppearanceOverlayStore.data(repo) ?: return null
        val colors = data.colors.takeIf { options.customColors }
        val image = data.image.takeIf { options.imageEffects }
        if (colors == null && image == null) return null
        return AppearanceOverlayData(colors = colors, image = image)
    }

    private fun buildVariantExport(
        variant: AdaptiveThemeVariantState,
        options: ThemePackageOptions,
    ): ThemePackageVariantExport {
        val source = variant.data
        val background = if (options.backgroundImage && source.hasBackground == true) {
            File(variant.backgroundPath).takeIf { it.isFile }
        } else {
            null
        }
        val card = if (options.homeCardImage && source.hasCardBackground == true) {
            File(variant.cardBackgroundPath).takeIf { it.isFile }
        } else {
            null
        }
        val stickers = if (options.stickers) {
            variant.stickers.map { File(it.path) }.filter { it.isFile }
        } else {
            emptyList()
        }
        val backgroundFlag = when {
            !options.backgroundImage -> null
            source.hasBackground == false -> false
            source.hasBackground == true && background != null -> true
            // A missing file must not produce a package that claims to contain it.
            else -> null
        }
        val cardFlag = when {
            !options.homeCardImage -> null
            source.hasCardBackground == false -> false
            source.hasCardBackground == true && card != null -> true
            else -> null
        }
        val stickerTransforms = when {
            !options.stickers -> null
            source.homeCardStickerTransforms == null -> null
            source.homeCardStickerTransforms.isEmpty() -> source.homeCardStickerTransforms
            stickers.isEmpty() -> null
            else -> source.homeCardStickerTransforms
        }
        val data = source.copy(
            themeMode = if (options.colors) source.themeMode else null,
            miuixMonet = if (options.colors) source.miuixMonet else null,
            keyColor = if (options.colors) source.keyColor else null,
            colorStyle = if (options.colors) source.colorStyle else null,
            colorSpec = if (options.colors) source.colorSpec else null,
            cardBackgroundAlphaPercent = if (options.backgroundAppearance) {
                source.cardBackgroundAlphaPercent
            } else {
                null
            },
            customBackgroundBlurPercent = if (options.backgroundAppearance) {
                source.customBackgroundBlurPercent
            } else {
                null
            },
            customBackgroundScrimPercent = if (options.backgroundAppearance) {
                source.customBackgroundScrimPercent
            } else {
                null
            },
            hasBackground = backgroundFlag,
            hasCardBackground = cardFlag,
            hideHomeCardStatusText = if (options.homeCardDisplay) {
                source.hideHomeCardStatusText
            } else {
                null
            },
            hideHomeCardDecoration = if (options.homeCardDisplay) {
                source.hideHomeCardDecoration
            } else {
                null
            },
            homeLayout = if (options.homeCardDisplay) source.homeLayout else null,
            homeStatusAlignment = if (options.homeCardDisplay) source.homeStatusAlignment else null,
            homeStatusScale = if (options.homeCardDisplay) source.homeStatusScale else null,
            homePillScale = if (options.homeCardDisplay) source.homePillScale else null,
            homeStatusWidthFraction = if (options.homeCardDisplay) {
                source.homeStatusWidthFraction
            } else {
                null
            },
            homeCardImageStyle = if (options.homeCardDisplay) source.homeCardImageStyle else null,
            showHomeCardImageWithoutBackground = if (options.homeCardDisplay) {
                source.showHomeCardImageWithoutBackground
            } else {
                null
            },
            homeCardStickerTransforms = stickerTransforms,
            uiMode = if (options.interfaceMode) repo.uiMode else null,
            // The chrome and behavior switches are global settings, not per light/dark
            // appearance, so both halves always carry the live values.
            enableBlur = if (options.interfaceSwitches) repo.enableBlur else null,
            enableProgressiveBlur = if (options.interfaceSwitches) {
                repo.enableProgressiveBlur
            } else {
                null
            },
            enableFloatingBottomBar = if (options.interfaceSwitches) {
                repo.enableFloatingBottomBar
            } else {
                null
            },
            enableFloatingBottomBarBlur = if (options.interfaceSwitches) {
                repo.enableFloatingBottomBarBlur
            } else {
                null
            },
            enableNavigationBadge = if (options.interfaceSwitches) {
                repo.enableNavigationBadge
            } else {
                null
            },
            enablePredictiveBack = if (options.interactionBehavior) {
                repo.enablePredictiveBack
            } else {
                null
            },
            enableSwipeDismiss = if (options.interactionBehavior) {
                repo.enableSwipeDismiss
            } else {
                null
            },
            pagerInterceptionMode = if (options.interactionBehavior) repo.pagerInterceptionMode else null,
            pageScale = if (options.interactionBehavior) repo.pageScale else null,
            moduleDescriptionMaxLines = if (options.moduleDisplay) {
                repo.moduleDescriptionMaxLines
            } else {
                null
            },
        )
        return ThemePackageVariantExport(
            data = data,
            backgroundFile = background,
            cardBackgroundFile = card,
            cardStickerFiles = stickers,
        )
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun deactivateAdaptiveTheme() {
        if (!AdaptiveThemeStore.isActive(repo)) return
        AdaptiveThemeStore.clear(repo)
    }

    private fun isSystemDarkMode(): Boolean =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    private fun isEffectiveDarkMode(): Boolean {
        val mode = ColorMode.fromValue(repo.themeMode)
        return mode.isDark || (mode.isSystem && isSystemDarkMode())
    }

    // Store packages are built for the source manager and may embed its brand in user-facing
    // words (home title, page labels and therefore the bottom bar tabs, settings rows). Rewrite
    // that brand to this manager's name at apply time; every other word and every key is kept.
    private val brandUltraPattern = Regex("(?i)sukisu\\s*ultra")
    private val brandPattern = Regex("(?i)sukisu")

    private fun rebrandThemeText(text: String): String =
        if (!brandPattern.containsMatchIn(text)) {
            text
        } else {
            brandPattern.replace(brandUltraPattern.replace(text, "KernelSU"), "KernelSU")
        }
}
