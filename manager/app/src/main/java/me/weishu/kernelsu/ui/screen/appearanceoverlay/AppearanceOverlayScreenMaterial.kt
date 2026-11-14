package me.weishu.kernelsu.ui.screen.appearanceoverlay

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedItemContainer
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.util.AppearanceColorMode
import me.weishu.kernelsu.ui.util.AppearanceColorToken
import me.weishu.kernelsu.ui.util.AppearanceEffectKey
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.AppearanceOverlayStore
import me.weishu.kernelsu.ui.util.colorTokenValue
import me.weishu.kernelsu.ui.util.effectiveColorToken
import me.weishu.kernelsu.ui.util.withColorToken
import me.weishu.kernelsu.ui.util.withImageEffect
import kotlin.math.roundToInt

@Composable
internal fun AppearanceOverlayScreenMaterial(
    data: AppearanceOverlayData,
    actions: AppearanceOverlayActions,
    backgroundBlurred: Boolean,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )
    var modeIndex by rememberSaveable { mutableIntStateOf(AppearanceColorMode.SHARED.ordinal) }
    val mode = AppearanceColorMode.entries[modeIndex]
    val effects = data.image?.background
    val sharpenSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var sharpenDraft by remember { mutableFloatStateOf(effects?.sharpen ?: 0f) }
    var saturationDraft by remember { mutableFloatStateOf(effects?.saturation ?: 100f) }
    var contrastDraft by remember { mutableFloatStateOf(effects?.contrast ?: 100f) }
    var brightnessDraft by remember { mutableFloatStateOf(effects?.brightness ?: 0f) }
    LaunchedEffect(effects?.sharpen) {
        sharpenDraft = effects?.sharpen ?: 0f
    }
    LaunchedEffect(effects?.saturation) {
        saturationDraft = effects?.saturation ?: 100f
    }
    LaunchedEffect(effects?.contrast) {
        contrastDraft = effects?.contrast ?: 100f
    }
    LaunchedEffect(effects?.brightness) {
        brightnessDraft = effects?.brightness ?: 0f
    }

    fun commitEffect(key: AppearanceEffectKey, value: Float, default: Float) {
        actions.onUpdate(data.withImageEffect(key, value.takeUnless { it == default }))
    }

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = { TopBarBackButton(onClick = actions.onBack) },
                title = { Text(stringResource(R.string.appearance_page_title)) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                actions = {
                    IconButton(onClick = actions.onResetAll) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = stringResource(R.string.appearance_clear_all),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SegmentedItemContainer {
                            Text(
                                text = stringResource(R.string.appearance_page_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            )
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.appearance_color_mode),
                            summary = stringResource(R.string.appearance_color_mode_summary),
                            icon = Icons.Rounded.Colorize,
                        )
                    }
                    item {
                        SegmentedItemContainer {
                            Column {
                                AppearanceColorMode.entries.forEachIndexed { index, entry ->
                                    SegmentedListItem(
                                        selected = modeIndex == index,
                                        onClick = { modeIndex = index },
                                        headlineContent = { Text(stringResource(entry.titleRes())) },
                                        leadingContent = {
                                            RadioButton(
                                                selected = modeIndex == index,
                                                onClick = null,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.appearance_section_text_colors),
                            summary = stringResource(
                                R.string.appearance_section_text_colors_summary,
                            ),
                            icon = Icons.Rounded.Colorize,
                        )
                    }
                    val tokens = listOf(
                        AppearanceColorToken.TEXT_PRIMARY,
                        AppearanceColorToken.TEXT_SECONDARY,
                        AppearanceColorToken.TEXT_DISABLED,
                        AppearanceColorToken.ON_ACCENT,
                    )
                    tokens.forEach { token ->
                        item(key = token.name) {
                            OverlayColorRow(
                                data = data,
                                mode = mode,
                                token = token,
                                actions = actions,
                            )
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.appearance_section_palette),
                            summary = stringResource(R.string.appearance_section_palette_summary),
                            icon = Icons.Rounded.Palette,
                        )
                    }
                    val tokens = listOf(
                        AppearanceColorToken.ACCENT,
                        AppearanceColorToken.SURFACE,
                        AppearanceColorToken.CARD,
                        AppearanceColorToken.OUTLINE,
                        AppearanceColorToken.ERROR,
                    )
                    tokens.forEach { token ->
                        item(key = token.name) {
                            OverlayColorRow(
                                data = data,
                                mode = mode,
                                token = token,
                                actions = actions,
                            )
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.appearance_section_image_effects),
                            summary = stringResource(
                                R.string.appearance_section_image_effects_summary,
                            ),
                            icon = Icons.Rounded.Tune,
                        )
                    }
                    item {
                        SegmentedItemContainer {
                            Column {
                                EffectSliderBlock(
                                    title = stringResource(R.string.appearance_effect_sharpen),
                                    summary = stringResource(
                                        R.string.appearance_effect_sharpen_summary,
                                    ),
                                    value = sharpenDraft,
                                    valueRange = 0f..100f,
                                    enabled = sharpenSupported,
                                    onValueChange = { sharpenDraft = it },
                                    onValueChangeFinished = {
                                        val rounded = sharpenDraft.roundToInt().toFloat()
                                        sharpenDraft = rounded
                                        commitEffect(
                                            AppearanceEffectKey.SHARPEN,
                                            rounded,
                                            0f,
                                        )
                                    },
                                    onReset = {
                                        sharpenDraft = 0f
                                        commitEffect(AppearanceEffectKey.SHARPEN, 0f, 0f)
                                    },
                                )
                                if (!sharpenSupported) {
                                    Text(
                                        text = stringResource(
                                            R.string.appearance_overlay_sharpen_api_notice,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp,
                                        ),
                                    )
                                } else if (backgroundBlurred && sharpenDraft > 0f) {
                                    Text(
                                        text = stringResource(
                                            R.string.appearance_effect_sharpen_blur_notice,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp,
                                        ),
                                    )
                                }
                                HorizontalDivider()
                                EffectSliderBlock(
                                    title = stringResource(
                                        R.string.appearance_effect_saturation,
                                    ),
                                    summary = stringResource(
                                        R.string.appearance_effect_saturation_summary,
                                    ),
                                    value = saturationDraft,
                                    onValueChange = { saturationDraft = it },
                                    onValueChangeFinished = {
                                        val rounded = saturationDraft.roundToInt().toFloat()
                                        saturationDraft = rounded
                                        commitEffect(
                                            AppearanceEffectKey.SATURATION,
                                            rounded,
                                            100f,
                                        )
                                    },
                                    onReset = {
                                        saturationDraft = 100f
                                        commitEffect(
                                            AppearanceEffectKey.SATURATION,
                                            100f,
                                            100f,
                                        )
                                    },
                                )
                                HorizontalDivider()
                                EffectSliderBlock(
                                    title = stringResource(R.string.appearance_effect_contrast),
                                    summary = stringResource(
                                        R.string.appearance_effect_contrast_summary,
                                    ),
                                    value = contrastDraft,
                                    onValueChange = { contrastDraft = it },
                                    onValueChangeFinished = {
                                        val rounded = contrastDraft.roundToInt().toFloat()
                                        contrastDraft = rounded
                                        commitEffect(
                                            AppearanceEffectKey.CONTRAST,
                                            rounded,
                                            100f,
                                        )
                                    },
                                    onReset = {
                                        contrastDraft = 100f
                                        commitEffect(
                                            AppearanceEffectKey.CONTRAST,
                                            100f,
                                            100f,
                                        )
                                    },
                                )
                                HorizontalDivider()
                                EffectSliderBlock(
                                    title = stringResource(
                                        R.string.appearance_effect_brightness,
                                    ),
                                    summary = stringResource(
                                        R.string.appearance_effect_brightness_summary,
                                    ),
                                    value = brightnessDraft,
                                    valueRange = -100f..100f,
                                    onValueChange = { brightnessDraft = it },
                                    onValueChangeFinished = {
                                        val rounded = brightnessDraft.roundToInt().toFloat()
                                        brightnessDraft = rounded
                                        commitEffect(
                                            AppearanceEffectKey.BRIGHTNESS,
                                            rounded,
                                            0f,
                                        )
                                    },
                                    onReset = {
                                        brightnessDraft = 0f
                                        commitEffect(AppearanceEffectKey.BRIGHTNESS, 0f, 0f)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                        SegmentedListItem(
                            onClick = actions.onRequestClear,
                            headlineContent = {
                                Text(stringResource(R.string.appearance_clear_all))
                            },
                            supportingContent = {
                                Text(stringResource(R.string.appearance_clear_all_summary))
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    summary: String,
    icon: ImageVector,
) {
    SegmentedListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

@Composable
private fun OverlayColorRow(
    data: AppearanceOverlayData,
    mode: AppearanceColorMode,
    token: AppearanceColorToken,
    actions: AppearanceOverlayActions,
) {
    val explicit = data.colorTokenValue(mode, token)
    val effective = data.effectiveColorToken(mode, token)
    val summary = when {
        explicit != null -> explicit
        mode != AppearanceColorMode.SHARED && effective != null ->
            stringResource(R.string.appearance_shared_value, effective.second)
        else -> stringResource(R.string.appearance_follow_theme)
    }
    SegmentedListItem(
        onClick = { actions.onPickColor(OverlayColorTarget(mode, token)) },
        headlineContent = { Text(stringResource(token.titleRes())) },
        supportingContent = { Text(summary) },
        leadingContent = {
            ColorSwatch(
                color = effective?.second
                    ?.let(AppearanceOverlayStore::parseColorValue)
                    ?.let(::Color),
                modifier = Modifier.padding(end = 10.dp),
            )
        },
        trailingContent = if (explicit != null) {
            {
                IconButton(
                    onClick = { actions.onUpdate(data.withColorToken(mode, token, null)) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = stringResource(R.string.appearance_override_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun ColorSwatch(color: Color?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color ?: MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
    )
}

@Composable
private fun EffectSliderBlock(
    title: String,
    summary: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onReset: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 20f..200f,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value.roundToInt().toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp),
            )
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.appearance_effect_reset))
            }
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            enabled = enabled,
        )
    }
}
