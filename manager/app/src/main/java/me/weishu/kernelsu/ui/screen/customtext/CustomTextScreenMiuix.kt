package me.weishu.kernelsu.ui.screen.customtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.theme.CustomTextGroup
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.customTextSummary
import me.weishu.kernelsu.ui.util.BlurredBar
import me.weishu.kernelsu.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import me.weishu.kernelsu.ui.component.miuix.AppScaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Undo
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
internal fun CustomTextScreenMiuix(
    customTexts: Map<String, String>,
    onBack: () -> Unit,
    onItemClick: (CustomTextKey) -> Unit,
    onResetGroup: (CustomTextGroup) -> Unit,
    onResetAll: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val barColor = if (blurActive || hasCustomBackground) Color.Transparent else colorScheme.surface

    AppScaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = customText(CustomTextKey.PAGE_CUSTOM_TEXTS),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                                tint = colorScheme.onBackground,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onResetAll) {
                            Icon(
                                imageVector = MiuixIcons.Undo,
                                contentDescription = stringResource(R.string.custom_text_restore_all),
                                tint = colorScheme.onBackground,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                var isFirstGroup = true
                CustomTextGroup.entries.forEach { group ->
                    val itemsInGroup = CustomTextKey.entries.filter { it.group == group }
                    if (itemsInGroup.isNotEmpty()) {
                        val isFirst = isFirstGroup
                        isFirstGroup = false
                        item(key = "title-${group.name}") {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                SmallTitle(
                                    text = stringResource(group.titleRes),
                                    modifier = Modifier.padding(
                                        top = if (isFirst) 12.dp else 16.dp,
                                    ),
                                    insideMargin = PaddingValues(
                                        start = 16.dp,
                                        end = 48.dp,
                                        bottom = 8.dp,
                                    ),
                                )
                                IconButton(
                                    onClick = { onResetGroup(group) },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Undo,
                                        contentDescription = stringResource(
                                            R.string.custom_text_restore_default,
                                        ),
                                        tint = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                        item(key = "card-${group.name}") {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                itemsInGroup.forEach { key ->
                                    ArrowPreference(
                                        title = stringResource(key.nameRes),
                                        summary = customTextSummary(key, customTexts[key.key]),
                                        onClick = { onItemClick(key) },
                                    )
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
}
