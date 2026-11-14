package me.weishu.kernelsu.ui.screen.colorpalette

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TopAppBar as MaterialTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.settings.ThemePackageOptions
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.util.AdaptiveThemeDraft
import me.weishu.kernelsu.ui.util.AdaptiveThemeState
import me.weishu.kernelsu.ui.util.AdaptiveThemeStore
import me.weishu.kernelsu.ui.util.AdaptiveThemeVariantState
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.ThemePackageStorage
import me.weishu.kernelsu.ui.util.ThemeVariantData
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

/**
 * Captures two complete appearances as an adaptive theme. Each capture keeps its own background,
 * card image, stickers and style values; the export writes them as a v3 package.
 */
@Composable
fun AdaptiveThemeBuilderScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val viewModel = viewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val repo = remember { SettingsRepositoryImpl() }
    val scope = rememberCoroutineScope()

    var draft by remember { mutableStateOf(AdaptiveThemeStore.loadDraft(repo)) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ThemePackageStorage.THEME_MIME_TYPE),
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = viewModel.createCurrentThemePackage(
                    ThemePackageOptions(),
                    preferDraft = true,
                )
                val file = result.getOrNull()
                val copied = file != null && withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            file.inputStream().use { input -> input.copyTo(output) }
                        } ?: error("No output stream")
                    }.isSuccess
                }
                val message = if (copied) R.string.theme_export_success else R.string.theme_export_failed
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                file?.delete()
            }
        }
    }

    fun refreshAdaptiveAppearance() {
        scope.launch {
            AdaptiveThemeStore.sync(repo, effectiveDarkMode(context, repo))
            viewModel.refresh()
            draft = AdaptiveThemeStore.loadDraft(repo)
        }
    }

    LaunchedEffect(Unit) { refreshAdaptiveAppearance() }
    LaunchedEffect(isSystemInDarkTheme()) { refreshAdaptiveAppearance() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshAdaptiveAppearance() }

    val currentMode = ColorMode.fromValue(state.themeMode)
    val effectiveDark = currentMode.isDark ||
        (currentMode.isSystem && isSystemInDarkTheme())
    val nextStep = when {
        draft?.complete == true -> R.string.theme_adaptive_draft_ready
        draft?.light != null -> R.string.theme_adaptive_next_dark
        draft?.dark != null -> R.string.theme_adaptive_next_light_missing
        else -> R.string.theme_adaptive_next_light
    }

    fun capture(dark: Boolean) {
        scope.launch {
            val variant = withContext(Dispatchers.IO) { buildDraftVariant(context, repo, dark) }
            if (variant == null) {
                Toast.makeText(context, R.string.theme_adaptive_capture_failed, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            val current = AdaptiveThemeStore.loadDraft(repo) ?: AdaptiveThemeDraft()
            val updated = if (dark) current.copy(dark = variant) else current.copy(light = variant)
            AdaptiveThemeStore.saveDraft(repo, updated)
            draft = updated
            Toast.makeText(
                context,
                context.getString(
                    R.string.theme_adaptive_capture_success,
                    context.getString(
                        if (dark) R.string.theme_adaptive_draft_dark
                        else R.string.theme_adaptive_draft_light,
                    ),
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun clearDraft() {
        scope.launch {
            AdaptiveThemeStore.clearDraft(repo)
            withContext(Dispatchers.IO) {
                ImageStorage.clearAdaptiveBackground(context, dark = false, draft = true)
                ImageStorage.clearAdaptiveBackground(context, dark = true, draft = true)
                ImageStorage.clearAdaptiveHomeCardImage(context, dark = false, draft = true)
                ImageStorage.clearAdaptiveHomeCardImage(context, dark = true, draft = true)
                ImageStorage.clearAdaptiveHomeCardStickers(context, dark = false, draft = true)
                ImageStorage.clearAdaptiveHomeCardStickers(context, dark = true, draft = true)
            }
            draft = null
            Toast.makeText(context, R.string.theme_adaptive_draft_cleared, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun applyDraft() {
        scope.launch {
            val current = AdaptiveThemeStore.loadDraft(repo)
            if (current?.complete != true) {
                Toast.makeText(context, R.string.theme_adaptive_draft_incomplete, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            val light = withContext(Dispatchers.IO) {
                promoteDraftVariant(context, current.light, dark = false)
            }
            val darkVariant = withContext(Dispatchers.IO) {
                promoteDraftVariant(context, current.dark, dark = true)
            }
            if (light == null || darkVariant == null) {
                Toast.makeText(context, R.string.theme_adaptive_capture_failed, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            val applied = AdaptiveThemeState(light = light, dark = darkVariant)
            AdaptiveThemeStore.save(repo, applied)
            val dark = effectiveDarkMode(context, repo)
            AdaptiveThemeStore.apply(repo, applied.variant(dark))
            repo.adaptiveAppliedMode = if (dark) 1 else 0
            refreshAdaptiveAppearance()
            Toast.makeText(context, R.string.theme_adaptive_applied, Toast.LENGTH_SHORT).show()
        }
    }

    val pageContainer = MaterialTheme.colorScheme.surface
    ExpressiveScaffold(
        containerColor = pageContainer,
        topBar = {
            MaterialTopAppBar(
                navigationIcon = { TopBarBackButton(onClick = { navigator.pop() }) },
                title = { MaterialText(stringResource(R.string.theme_adaptive_builder_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pageContainer,
                    scrolledContainerColor = pageContainer,
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "intro") {
                BuilderCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MaterialIcon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        MaterialText(
                            text = stringResource(R.string.theme_adaptive_builder_summary),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    MaterialText(
                        text = stringResource(
                            R.string.theme_adaptive_current_mode,
                            stringResource(
                                if (effectiveDark) R.string.theme_adaptive_mode_dark
                                else R.string.theme_adaptive_mode_light,
                            ),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    MaterialText(
                        text = stringResource(nextStep),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    MaterialText(
                        text = stringResource(R.string.theme_adaptive_builder_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { navigator.pop() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MaterialText(stringResource(R.string.theme_adaptive_edit_appearance))
                    }
                }
            }
            item(key = "light") {
                VariantCaptureCard(
                    title = stringResource(R.string.theme_adaptive_draft_light),
                    captured = draft?.light != null,
                    variant = draft?.light,
                    dark = false,
                    icon = Icons.Rounded.LightMode,
                    onCapture = { capture(dark = false) },
                )
            }
            item(key = "dark") {
                VariantCaptureCard(
                    title = stringResource(R.string.theme_adaptive_draft_dark),
                    captured = draft?.dark != null,
                    variant = draft?.dark,
                    dark = true,
                    icon = Icons.Rounded.DarkMode,
                    onCapture = { capture(dark = true) },
                )
            }
            if (draft?.complete == true) {
                item(key = "ready") {
                    BuilderCard {
                        MaterialText(
                            text = stringResource(R.string.theme_adaptive_draft_ready),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    exportLauncher.launch(ThemePackageStorage.packageFileName())
                                },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.weight(1f),
                            ) {
                                MaterialIcon(
                                    imageVector = Icons.Rounded.FileUpload,
                                    contentDescription = null,
                                )
                                Spacer(Modifier.width(8.dp))
                                MaterialText(
                                    text = stringResource(R.string.theme_adaptive_builder_export),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            OutlinedButton(
                                onClick = { applyDraft() },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.weight(1f),
                            ) {
                                MaterialIcon(
                                    imageVector = Icons.Rounded.FileDownload,
                                    contentDescription = null,
                                )
                                Spacer(Modifier.width(8.dp))
                                MaterialText(
                                    text = stringResource(R.string.theme_adaptive_builder_apply),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { navigator.push(Route.ThemeStoreAdaptiveUpload) },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MaterialIcon(
                                imageVector = Icons.Rounded.FileUpload,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            MaterialText(
                                text = stringResource(R.string.theme_adaptive_builder_publish),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        MaterialText(
                            text = stringResource(R.string.theme_adaptive_export_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (draft?.light != null || draft?.dark != null) {
                item(key = "clear") {
                    FilledTonalButton(
                        onClick = { clearDraft() },
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MaterialIcon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                        )
                        MaterialText(
                            text = stringResource(R.string.theme_adaptive_builder_clear),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuilderCard(content: @Composable () -> Unit) {
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun VariantCaptureCard(
    title: String,
    captured: Boolean,
    variant: AdaptiveThemeVariantState?,
    dark: Boolean,
    icon: ImageVector,
    onCapture: () -> Unit,
) {
    BuilderCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MaterialIcon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            MaterialText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (captured) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                contentColor = if (captured) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ) {
                MaterialText(
                    text = stringResource(
                        if (captured) R.string.theme_adaptive_captured
                        else R.string.theme_adaptive_not_captured,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        if (variant != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape = CircleShape,
                    color = variant.data.keyColor
                        ?.takeIf { it != 0 }
                        ?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.primary,
                ) {}
                MaterialText(
                    text = stringResource(
                        R.string.theme_adaptive_captured_summary,
                        stringResource(
                            if (variant.data.hasBackground == true) {
                                R.string.theme_adaptive_captured
                            } else {
                                R.string.theme_adaptive_not_captured
                            },
                        ),
                        stringResource(
                            if (variant.data.hasCardBackground == true) {
                                R.string.theme_adaptive_captured
                            } else {
                                R.string.theme_adaptive_not_captured
                            },
                        ),
                        variant.stickers.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(
            onClick = onCapture,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MaterialText(
                text = stringResource(
                    when {
                        captured && dark -> R.string.theme_adaptive_recapture_dark
                        captured -> R.string.theme_adaptive_recapture_light
                        dark -> R.string.theme_adaptive_capture_dark
                        else -> R.string.theme_adaptive_capture_light
                    },
                ),
            )
        }
    }
}

private fun effectiveDarkMode(context: Context, repo: SettingsRepository): Boolean {
    val mode = ColorMode.fromValue(repo.themeMode)
    val systemDark = context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    return mode.isDark || (mode.isSystem && systemDark)
}

private suspend fun buildDraftVariant(
    context: Context,
    repo: SettingsRepository,
    dark: Boolean,
): AdaptiveThemeVariantState? = withContext(Dispatchers.IO) {
    runCatching {
        val backgroundSource = when {
            repo.useSystemWallpaper ->
                ImageStorage.systemWallpaperFile(context).takeIf { it.isFile }

            repo.customBackgroundUri.isNotBlank() ->
                File(repo.customBackgroundUri).takeIf { it.isFile }

            else -> null
        }
        val backgroundPath = if (backgroundSource != null) {
            val staged = ImageStorage.stageFile(context, backgroundSource)
                ?: error("Could not stage the background")
            ImageStorage.replaceAdaptiveBackground(
                context,
                File(staged),
                dark,
                draft = true,
            ) ?: error("Could not save the background")
        } else {
            ImageStorage.clearAdaptiveBackground(context, dark, draft = true)
            ""
        }

        val cardSource = repo.homeCardImagePath
            .takeIf { it.isNotBlank() }
            ?.let { File(it).takeIf { file -> file.isFile } }
        val cardPath = if (cardSource != null) {
            val staged = ImageStorage.stageFile(context, cardSource)
                ?: error("Could not stage the card image")
            ImageStorage.replaceAdaptiveHomeCardImage(
                context,
                File(staged),
                dark,
                draft = true,
            ) ?: error("Could not save the card image")
        } else {
            ImageStorage.clearAdaptiveHomeCardImage(context, dark, draft = true)
            ""
        }

        ImageStorage.clearAdaptiveHomeCardStickers(context, dark, draft = true)
        val stickers = repo.homeCardStickers.mapIndexedNotNull { index, sticker ->
            val source = File(sticker.path).takeIf { it.isFile }
                ?: return@mapIndexedNotNull null
            val staged = ImageStorage.stageFile(context, source)
                ?: return@mapIndexedNotNull null
            ImageStorage.importAdaptiveHomeCardSticker(
                context,
                File(staged),
                dark,
                index,
                draft = true,
            )?.let { path ->
                HomeCardSticker(path = path, transform = sticker.transform.coerced())
            }
        }.take(HomeCardSticker.MAX_COUNT)

        AdaptiveThemeVariantState(
            data = ThemeVariantData(
                themeMode = repo.themeMode,
                miuixMonet = repo.miuixMonet,
                keyColor = repo.keyColor,
                colorStyle = repo.colorStyle,
                colorSpec = repo.colorSpec,
                cardBackgroundAlphaPercent = repo.cardBackgroundAlphaPercent,
                customBackgroundBlurPercent = repo.customBackgroundBlurPercent,
                customBackgroundScrimPercent = repo.customBackgroundScrimPercent,
                hasBackground = backgroundPath.isNotEmpty(),
                hasCardBackground = cardPath.isNotEmpty(),
                hideHomeCardStatusText = repo.hideHomeCardStatusText,
                hideHomeCardDecoration = repo.hideHomeCardDecoration,
                homeCardStickerTransforms = stickers.map { it.transform },
                homeLayout = repo.homeLayout,
                homeStatusAlignment = repo.homeStatusAlignment,
                homeStatusScale = repo.homeStatusScale,
                homePillScale = repo.homePillScale,
                homeStatusWidthFraction = repo.homeStatusWidthFraction,
                homeCardImageStyle = repo.homeCardImageStyle,
                showHomeCardImageWithoutBackground = repo.showHomeCardImageWithoutBackground,
                uiMode = repo.uiMode,
                enableFloatingBottomBar = repo.enableFloatingBottomBar,
                enableFloatingBottomBarBlur = repo.enableFloatingBottomBarBlur,
                enableNavigationBadge = repo.enableNavigationBadge,
                enablePredictiveBack = repo.enablePredictiveBack,
                enableSwipeDismiss = repo.enableSwipeDismiss,
                enableBlur = repo.enableBlur,
                enableProgressiveBlur = repo.enableProgressiveBlur,
                pagerInterceptionMode = repo.pagerInterceptionMode,
                pageScale = repo.pageScale,
                moduleDescriptionMaxLines = repo.moduleDescriptionMaxLines,
            ),
            backgroundPath = backgroundPath,
            cardBackgroundPath = cardPath,
            stickers = stickers,
        )
    }.getOrNull()
}

private suspend fun promoteDraftVariant(
    context: Context,
    variant: AdaptiveThemeVariantState?,
    dark: Boolean,
): AdaptiveThemeVariantState? = withContext(Dispatchers.IO) {
    if (variant == null) return@withContext null
    runCatching {
        val backgroundSource = variant.backgroundPath
            .takeIf { it.isNotBlank() }
            ?.let { File(it).takeIf { file -> file.isFile } }
        check(variant.data.hasBackground != true || backgroundSource != null) {
            "Draft background is missing"
        }
        val backgroundPath = backgroundSource
            ?.let { ImageStorage.stageFile(context, it) }
            ?.let { staged -> ImageStorage.replaceAdaptiveBackground(context, File(staged), dark) }
            ?: run {
                ImageStorage.clearAdaptiveBackground(context, dark)
                ""
            }
        val cardSource = variant.cardBackgroundPath
            .takeIf { it.isNotBlank() }
            ?.let { File(it).takeIf { file -> file.isFile } }
        check(variant.data.hasCardBackground != true || cardSource != null) {
            "Draft card background is missing"
        }
        val cardPath = cardSource
            ?.let { ImageStorage.stageFile(context, it) }
            ?.let { staged -> ImageStorage.replaceAdaptiveHomeCardImage(context, File(staged), dark) }
            ?: run {
                ImageStorage.clearAdaptiveHomeCardImage(context, dark)
                ""
            }
        ImageStorage.clearAdaptiveHomeCardStickers(context, dark)
        val stickers = variant.stickers.mapIndexedNotNull { index, sticker ->
            val staged = ImageStorage.stageFile(context, File(sticker.path))
                ?: return@mapIndexedNotNull null
            ImageStorage.importAdaptiveHomeCardSticker(
                context,
                File(staged),
                dark,
                index,
            )?.let { path -> HomeCardSticker(path = path, transform = sticker.transform) }
        }
        AdaptiveThemeVariantState(
            data = variant.data.copy(
                hasBackground = backgroundPath.isNotEmpty(),
                hasCardBackground = cardPath.isNotEmpty(),
                homeCardStickerTransforms = stickers.map { it.transform },
            ),
            backgroundPath = backgroundPath,
            cardBackgroundPath = cardPath,
            stickers = stickers,
        )
    }.getOrNull()
}
