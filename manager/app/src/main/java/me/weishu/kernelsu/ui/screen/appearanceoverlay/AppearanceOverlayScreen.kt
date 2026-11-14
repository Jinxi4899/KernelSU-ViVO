package me.weishu.kernelsu.ui.screen.appearanceoverlay

import android.graphics.Color as AndroidColor
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.theme.isInDarkTheme
import me.weishu.kernelsu.ui.util.AppearanceColorMode
import me.weishu.kernelsu.ui.util.AppearanceColorToken
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.AppearanceOverlayStore
import me.weishu.kernelsu.ui.util.colorTokenValue
import me.weishu.kernelsu.ui.util.effectiveColorToken
import me.weishu.kernelsu.ui.util.toOverlayColorString
import me.weishu.kernelsu.ui.util.withColorToken
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Immutable
internal data class OverlayColorTarget(
    val mode: AppearanceColorMode,
    val token: AppearanceColorToken,
)

internal data class AppearanceOverlayActions(
    val onBack: () -> Unit,
    val onUpdate: (AppearanceOverlayData) -> Unit,
    val onPickColor: (OverlayColorTarget) -> Unit,
    val onResetAll: () -> Unit,
    val onRequestClear: () -> Unit,
)

@StringRes
internal fun AppearanceColorToken.titleRes(): Int = when (this) {
    AppearanceColorToken.ACCENT -> R.string.appearance_color_accent
    AppearanceColorToken.ON_ACCENT -> R.string.appearance_color_on_accent
    AppearanceColorToken.TEXT_PRIMARY -> R.string.appearance_color_text_primary
    AppearanceColorToken.TEXT_SECONDARY -> R.string.appearance_color_text_secondary
    AppearanceColorToken.TEXT_DISABLED -> R.string.appearance_color_text_disabled
    AppearanceColorToken.SURFACE -> R.string.appearance_color_surface
    AppearanceColorToken.CARD -> R.string.appearance_color_card
    AppearanceColorToken.OUTLINE -> R.string.appearance_color_outline
    AppearanceColorToken.ERROR -> R.string.appearance_color_error
}

@StringRes
internal fun AppearanceColorMode.titleRes(): Int = when (this) {
    AppearanceColorMode.SHARED -> R.string.appearance_mode_shared
    AppearanceColorMode.LIGHT -> R.string.appearance_mode_light
    AppearanceColorMode.DARK -> R.string.appearance_mode_dark
}

@Composable
fun AppearanceOverlayScreen(onBack: () -> Unit) {
    val repo = remember { SettingsRepositoryImpl() }
    var data by remember { mutableStateOf(AppearanceOverlayStore.data(repo) ?: AppearanceOverlayData()) }
    var pickerTarget by remember { mutableStateOf<OverlayColorTarget?>(null) }
    val isDark = isInDarkTheme()
    val backgroundBlurred = remember { repo.customBackgroundBlurPercent > 0 }

    val clearTitle = stringResource(R.string.appearance_clear_all_confirm_title)
    val clearMessage = stringResource(R.string.appearance_clear_all_confirm_message)
    val clearDialog = rememberConfirmDialog(onConfirm = {
        AppearanceOverlayStore.clear(repo)
        data = AppearanceOverlayData()
    })

    fun update(updated: AppearanceOverlayData) {
        AppearanceOverlayStore.save(repo, updated)
        data = updated
    }

    fun clear() {
        AppearanceOverlayStore.clear(repo)
        data = AppearanceOverlayData()
    }

    val actions = AppearanceOverlayActions(
        onBack = onBack,
        onUpdate = { update(it) },
        onPickColor = { pickerTarget = it },
        onResetAll = { clear() },
        onRequestClear = { clearDialog.showConfirm(title = clearTitle, content = clearMessage) },
    )

    pickerTarget?.let { target ->
        val effective = data.effectiveColorToken(target.mode, target.token)
        val defaultArgb = when (target.mode) {
            AppearanceColorMode.LIGHT -> 0xFF000000.toInt()
            AppearanceColorMode.DARK -> 0xFFFFFFFF.toInt()
            AppearanceColorMode.SHARED -> if (isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        }
        val initialArgb = effective?.second
            ?.let(AppearanceOverlayStore::parseColorValue)
            ?: defaultArgb
        ColorPickerDialog(
            title = "${stringResource(target.token.titleRes())} · ${
                stringResource(target.mode.titleRes())
            }",
            initialColor = Color(initialArgb),
            clearable = data.colorTokenValue(target.mode, target.token) != null,
            onDismiss = { pickerTarget = null },
            onConfirm = { color ->
                update(
                    data.withColorToken(
                        target.mode,
                        target.token,
                        color.toArgb().toOverlayColorString(),
                    ),
                )
                pickerTarget = null
            },
            onClear = {
                update(data.withColorToken(target.mode, target.token, null))
                pickerTarget = null
            },
        )
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> AppearanceOverlayScreenMiuix(
            data = data,
            actions = actions,
            backgroundBlurred = backgroundBlurred,
        )

        UiMode.Material -> AppearanceOverlayScreenMaterial(
            data = data,
            actions = actions,
            backgroundBlurred = backgroundBlurred,
        )
    }
}

@Composable
private fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    clearable: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
    onClear: () -> Unit,
) {
    val miuix = LocalUiMode.current == UiMode.Miuix
    val initialArgb = initialColor.toArgb()
    val initialHsv = FloatArray(3).also { AndroidColor.colorToHSV(initialArgb, it) }

    var hue by rememberSaveable(initialArgb) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by rememberSaveable(initialArgb) { mutableFloatStateOf(initialHsv[1]) }
    var value by rememberSaveable(initialArgb) { mutableFloatStateOf(initialHsv[2]) }
    var alpha by rememberSaveable(initialArgb) {
        mutableFloatStateOf(AndroidColor.alpha(initialArgb) / 255f)
    }
    var hexText by rememberSaveable(initialArgb) {
        mutableStateOf(initialArgb.toOverlayColorString())
    }
    var hexFocused by remember { mutableStateOf(false) }
    var hexEditing by remember { mutableStateOf(false) }
    var hexError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val currentArgb = AndroidColor.HSVToColor(
        (alpha * 255f).roundToInt().coerceIn(0, 255),
        floatArrayOf(hue, saturation, value),
    )
    val currentColor = Color(currentArgb)

    LaunchedEffect(currentArgb, hexFocused, hexEditing) {
        if (!hexFocused && !hexEditing) {
            hexText = currentArgb.toOverlayColorString()
            hexError = false
        }
    }

    fun applyHex(raw: String) {
        val candidate = raw.trim()
        if (candidate.isEmpty()) {
            hexError = false
            return
        }
        val normalized = if (candidate.startsWith("#")) candidate else "#$candidate"
        val parsed = AppearanceOverlayStore.parseColorValue(normalized)
        if (parsed == null) {
            hexError = true
            return
        }
        hexError = false
        val hsv = FloatArray(3).also { AndroidColor.colorToHSV(parsed, it) }
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        alpha = AndroidColor.alpha(parsed) / 255f
    }

    val body: @Composable () -> Unit = {
        ColorPickerBody(
            miuix = miuix,
            hue = hue,
            saturation = saturation,
            value = value,
            alpha = alpha,
            currentColor = currentColor,
            hexText = hexText,
            hexError = hexError,
            onHexChange = {
                hexEditing = true
                hexText = it
                applyHex(it)
            },
            onHexFocusChange = {
                hexFocused = it
                if (!it) hexEditing = false
            },
            onHueChange = {
                focusManager.clearFocus()
                hexEditing = false
                hue = it
            },
            onSaturationValueChange = { newSaturation, newValue ->
                focusManager.clearFocus()
                hexEditing = false
                saturation = newSaturation
                value = newValue
            },
            onAlphaChange = {
                focusManager.clearFocus()
                hexEditing = false
                alpha = it
            },
        )
    }

    if (miuix) {
        OverlayDialog(
            show = true,
            title = title,
            onDismissRequest = onDismiss,
            insideMargin = DpSize(0.dp, 24.dp),
            content = {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    body()
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (clearable) {
                            MiuixTextButton(
                                text = stringResource(R.string.appearance_override_clear),
                                onClick = onClear,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        MiuixTextButton(
                            text = stringResource(R.string.cancel),
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                        )
                        MiuixTextButton(
                            text = stringResource(R.string.appearance_picker_apply),
                            onClick = { onConfirm(currentColor) },
                            modifier = Modifier.weight(1f),
                            enabled = !hexError,
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { MaterialText(title) },
            text = {
                Column {
                    body()
                    if (clearable) {
                        Spacer(Modifier.height(8.dp))
                        MaterialTextButton(onClick = onClear) {
                            MaterialText(stringResource(R.string.appearance_override_clear))
                        }
                    }
                }
            },
            confirmButton = {
                MaterialTextButton(
                    onClick = { onConfirm(currentColor) },
                    enabled = !hexError,
                ) {
                    MaterialText(stringResource(R.string.appearance_picker_apply))
                }
            },
            dismissButton = {
                MaterialTextButton(onClick = onDismiss) {
                    MaterialText(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ColorPickerBody(
    miuix: Boolean,
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    currentColor: Color,
    hexText: String,
    hexError: Boolean,
    onHexChange: (String) -> Unit,
    onHexFocusChange: (Boolean) -> Unit,
    onHueChange: (Float) -> Unit,
    onSaturationValueChange: (Float, Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
) {
    val onSurfaceColor = if (miuix) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val outline = if (miuix) {
        MiuixTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outline
    }
    val surface = if (miuix) {
        MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val primary = if (miuix) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val errorColor = if (miuix) {
        MiuixTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(currentColor),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                HexColorField(
                    value = hexText,
                    onValueChange = onHexChange,
                    onFocusChange = onHexFocusChange,
                    isError = hexError,
                    miuix = miuix,
                )
                if (hexError) {
                    PickerLabel(
                        text = stringResource(R.string.appearance_picker_hex_invalid),
                        color = errorColor,
                        miuix = miuix,
                    )
                }
                PickerLabel(
                    text = stringResource(
                        R.string.appearance_picker_alpha_value,
                        (alpha * 100f).roundToInt(),
                    ),
                    color = onSurfaceColor.copy(alpha = 0.7f),
                    miuix = miuix,
                )
            }
        }

        SaturationValueField(
            hue = hue,
            saturation = saturation,
            value = value,
            outline = outline,
            onChange = onSaturationValueChange,
        )

        GradientBar(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red,
                ),
            ),
            fraction = hue / 360f,
            thumbColor = surface,
            outline = outline,
            onChange = { onHueChange((it * 360f).coerceIn(0f, 360f)) },
        )
        PickerLabel(
            text = stringResource(R.string.appearance_picker_hue),
            color = onSurfaceColor.copy(alpha = 0.7f),
            miuix = miuix,
        )

        GradientBar(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.hsv(hue, saturation, value).copy(alpha = 0f),
                    Color.hsv(hue, saturation, value),
                ),
            ),
            fraction = alpha,
            thumbColor = surface,
            outline = outline,
            onChange = { onAlphaChange(it.coerceIn(0f, 1f)) },
        )
        PickerLabel(
            text = stringResource(R.string.appearance_picker_alpha),
            color = onSurfaceColor.copy(alpha = 0.7f),
            miuix = miuix,
        )

        PickerLabel(
            text = stringResource(R.string.appearance_picker_notice),
            color = primary,
            miuix = miuix,
        )
    }
}

@Composable
private fun HexColorField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    isError: Boolean,
    miuix: Boolean,
) {
    if (miuix) {
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.appearance_picker_hex),
            useLabelAsPlaceholder = true,
            singleLine = true,
            textStyle = MiuixTheme.textStyles.main.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
            ),
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it.isFocused) },
            label = { MaterialText(stringResource(R.string.appearance_picker_hex)) },
            singleLine = true,
            isError = isError,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
            ),
        )
    }
}

@Composable
private fun PickerLabel(
    text: String,
    color: Color,
    miuix: Boolean,
) {
    if (miuix) {
        MiuixText(
            text = text,
            color = color,
            style = MiuixTheme.textStyles.body2,
        )
    } else {
        MaterialText(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SaturationValueField(
    hue: Float,
    saturation: Float,
    value: Float,
    outline: Color,
    onChange: (Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = androidx.compose.ui.geometry.Offset(
                saturation * size.width,
                (1f - value) * size.height,
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.4f),
                radius = 9.dp.toPx(),
                center = center,
            )
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(hue) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onChange(
                            (down.position.x / size.width).coerceIn(0f, 1f),
                            (1f - down.position.y / size.height).coerceIn(0f, 1f),
                        )
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    onChange(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (1f - change.position.y / size.height).coerceIn(0f, 1f),
                                    )
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                },
        )
    }
}

@Composable
private fun GradientBar(
    brush: Brush,
    fraction: Float,
    thumbColor: Color,
    outline: Color,
    onChange: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(CircleShape)
            .background(brush)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onChange((down.position.x / size.width).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                onChange((change.position.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val x = fraction.coerceIn(0f, 1f) * size.width
            drawCircle(
                color = outline,
                radius = 10.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f),
            )
            drawCircle(
                color = thumbColor,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f),
            )
        }
    }
}
