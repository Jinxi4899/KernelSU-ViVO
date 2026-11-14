package me.weishu.kernelsu.ui.screen.customtext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.theme.CustomTextArg
import me.weishu.kernelsu.ui.theme.CustomTextGroup
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.joinTemplate
import me.weishu.kernelsu.ui.theme.splitTemplate
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun CustomTextScreen() {
    val navigator = LocalNavigator.current
    val repo = remember { SettingsRepositoryImpl() }
    var customTexts by remember { mutableStateOf(repo.customTexts) }
    var editingKey by remember { mutableStateOf<CustomTextKey?>(null) }
    var editTextValue by remember { mutableStateOf("") }

    val resetAllTitle = stringResource(R.string.custom_text_restore_all)
    val resetAllMessage = stringResource(R.string.custom_text_restore_all_confirm)
    val resetAllDialog = rememberConfirmDialog(onConfirm = {
        repo.customTexts = emptyMap()
        repo.customHomeTitle = ""
        customTexts = emptyMap()
    })

    val onItemClick: (CustomTextKey) -> Unit = { key ->
        editingKey = key
        editTextValue = customTexts[key.key].orEmpty()
    }

    fun applyText(key: CustomTextKey, value: String) {
        val trimmed = value.trim()
        val updated = customTexts.toMutableMap()
        if (trimmed.isEmpty()) updated.remove(key.key) else updated[key.key] = trimmed
        repo.customTexts = updated
        if (key == CustomTextKey.HOME_TITLE) repo.customHomeTitle = trimmed
        customTexts = updated
    }

    fun resetGroup(group: CustomTextGroup) {
        val keys = CustomTextKey.entries
            .filter { it.group == group }
            .map { it.key }
            .toSet()
        val updated = customTexts - keys
        repo.customTexts = updated
        if (group == CustomTextGroup.HOME) repo.customHomeTitle = ""
        customTexts = updated
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> CustomTextScreenMiuix(
            customTexts = customTexts,
            onBack = dropUnlessResumed { navigator.pop() },
            onItemClick = onItemClick,
            onResetGroup = { resetGroup(it) },
            onResetAll = { resetAllDialog.showConfirm(title = resetAllTitle, content = resetAllMessage) },
        )

        UiMode.Material -> CustomTextScreenMaterial(
            customTexts = customTexts,
            onBack = dropUnlessResumed { navigator.pop() },
            onItemClick = onItemClick,
            onResetGroup = { resetGroup(it) },
            onResetAll = { resetAllDialog.showConfirm(title = resetAllTitle, content = resetAllMessage) },
        )
    }

    editingKey?.let { key ->
        CustomTextEditDialog(
            key = key,
            initialValue = editTextValue,
            onDismiss = { editingKey = null },
            onConfirm = { value ->
                applyText(key, value)
                editingKey = null
            },
            onRestoreDefault = {
                applyText(key, "")
                editingKey = null
            },
        )
    }
}

@Composable
private fun CustomTextEditDialog(
    key: CustomTextKey,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    val defaultText = stringResource(key.defaultRes)
    val hasVariables = key.formatArgs.isNotEmpty()
    val defaultParts = remember(key, defaultText) { key.splitTemplate(defaultText) }
    var text by remember(key, initialValue) { mutableStateOf(initialValue) }
    var parts by remember(key, initialValue) {
        mutableStateOf(key.splitTemplate(initialValue.ifBlank { defaultText }))
    }

    // The placeholder itself is never edited: the app inserts it between the literal pieces.
    val assembled = when {
        !hasVariables -> text
        parts.all { it.isBlank() } || parts == defaultParts -> ""
        else -> key.joinTemplate(parts)
    }

    fun updatePart(index: Int, value: String) {
        parts = parts.toMutableList().also { it[index] = value }
    }

    val hintRes = if (hasVariables) R.string.custom_text_variable_hint else R.string.custom_text_edit_hint

    when (LocalUiMode.current) {
        UiMode.Miuix -> {
            WindowDialog(
                show = true,
                title = stringResource(R.string.custom_text_edit_title) +
                    " · " + stringResource(key.nameRes),
                onDismissRequest = onDismiss,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (hasVariables) {
                            key.formatArgs.forEachIndexed { index, arg ->
                                TextField(
                                    value = parts[index],
                                    onValueChange = { updatePart(index, it) },
                                    label = defaultParts[index],
                                    useLabelAsPlaceholder = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                MiuixVariableRow(arg = arg)
                            }
                            TextField(
                                value = parts.last(),
                                onValueChange = { updatePart(parts.lastIndex, it) },
                                label = defaultParts.last(),
                                useLabelAsPlaceholder = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                label = defaultText,
                                useLabelAsPlaceholder = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Text(
                            text = stringResource(hintRes),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TextButton(
                                text = stringResource(R.string.custom_text_restore_default),
                                onClick = onRestoreDefault,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                text = stringResource(R.string.confirm),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                onClick = { onConfirm(assembled) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                },
            )
        }

        UiMode.Material -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { MaterialText(stringResource(key.nameRes)) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (hasVariables) {
                            key.formatArgs.forEachIndexed { index, arg ->
                                OutlinedTextField(
                                    value = parts[index],
                                    onValueChange = { updatePart(index, it) },
                                    label = {
                                        MaterialText(stringResource(R.string.custom_text_part_before))
                                    },
                                    placeholder = { MaterialText(defaultParts[index]) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                MaterialVariableRow(
                                    arg = arg,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                            OutlinedTextField(
                                value = parts.last(),
                                onValueChange = { updatePart(parts.lastIndex, it) },
                                label = {
                                    MaterialText(stringResource(R.string.custom_text_part_after))
                                },
                                placeholder = { MaterialText(defaultParts.last()) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = { MaterialText(defaultText) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        MaterialText(
                            text = stringResource(hintRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    MaterialTextButton(onClick = { onConfirm(assembled) }) {
                        MaterialText(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    Row {
                        MaterialTextButton(onClick = onRestoreDefault) {
                            MaterialText(stringResource(R.string.custom_text_restore_default))
                        }
                        MaterialTextButton(onClick = onDismiss) {
                            MaterialText(stringResource(R.string.cancel))
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun MiuixVariableRow(arg: CustomTextArg) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.custom_text_variable),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = stringResource(arg.labelRes),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MiuixTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun MaterialVariableRow(arg: CustomTextArg, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MaterialText(
            text = stringResource(R.string.custom_text_variable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            MaterialText(
                text = stringResource(arg.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
