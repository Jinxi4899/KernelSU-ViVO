package me.weishu.kernelsu.ui.screen.customtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.theme.CustomTextGroup
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.customTextSummary

@Composable
internal fun CustomTextScreenMaterial(
    customTexts: Map<String, String>,
    onBack: () -> Unit,
    onItemClick: (CustomTextKey) -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
    onResetAll: () -> Unit,
) {
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()

    ExpressiveScaffold(
        containerColor = if (hasCustomBackground) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        topBar = {
            TopAppBar(
                title = { Text(customText(CustomTextKey.PAGE_CUSTOM_TEXTS)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onResetAll) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = stringResource(R.string.custom_text_restore_all),
                        )
                    }
                },
                colors = expressiveTopAppBarColors(
                    containerColor = if (hasCustomBackground) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    scrolledContainerColor = if (hasCustomBackground) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                ),
                windowInsets = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CustomTextGroup.entries.forEach { group ->
                val itemsInGroup = CustomTextKey.entries.filter { it.group == group }
                if (itemsInGroup.isNotEmpty()) {
                    item(key = "group-${group.name}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(group.titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { onResetGroup(group) }) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = stringResource(
                                        R.string.custom_text_restore_default,
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        SegmentedColumn {
                            itemsInGroup.forEach { key ->
                                item(key = key.key) {
                                    SegmentedListItem(
                                        onClick = { onItemClick(key) },
                                        headlineContent = {
                                            Text(stringResource(key.nameRes))
                                        },
                                        supportingContent = {
                                            Text(
                                                customTextSummary(key, customTexts[key.key]),
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "bottom-inset") {
                Spacer(
                    Modifier.height(
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 16.dp,
                    ),
                )
            }
        }
    }
}
