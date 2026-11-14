package me.weishu.kernelsu.ui.component.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.theme.backgroundAwareColor
import me.weishu.kernelsu.ui.theme.customText
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailDefaults
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun NavigationRailMiuix(
    navigationBadge: NavigationBadgeState,
    modifier: Modifier = Modifier,
) {
    val fullFeatured = Natives.isFullFeatured()
    if (!fullFeatured) return

    val mainState = LocalMainPagerState.current
    val bottomBarIcons = rememberBottomBarIconPaths()

    val items = BottomBarDestination.entries.map { destination ->
        Triple(
            customText(destination.textKey),
            destination.icon,
            bottomBarIcons[destination.key],
        )
    }
    val settingsRepo = remember { SettingsRepositoryImpl() }
    val state = rememberNavigationRailState(
        initialValue = if (settingsRepo.navigationRailExpanded) {
            NavigationRailValue.Expanded
        } else {
            NavigationRailValue.Collapsed
        },
    )
    LaunchedEffect(state.currentValue) {
        settingsRepo.navigationRailExpanded = state.isExpanded
    }

    NavigationRail(
        modifier = modifier,
        state = state,
        color = backgroundAwareColor(MiuixTheme.colorScheme.surface),
        expandContentDescription = stringResource(R.string.nav_rail_expand),
        collapseContentDescription = stringResource(R.string.nav_rail_collapse),
    ) {
        items.forEachIndexed { index, (label, icon, iconPath) ->
            val selected = mainState.selectedPage == index
            if (iconPath.isNullOrBlank()) {
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        mainState.animateToPage(index)
                    },
                    icon = icon,
                    label = label,
                    badge = navigationBadgeFor(index, navigationBadge),
                )
            } else {
                MiuixCustomNavigationRailItem(
                    selected = selected,
                    expanded = state.isExpanded,
                    onClick = { mainState.animateToPage(index) },
                    icon = icon,
                    iconPath = iconPath,
                    label = label,
                    badge = navigationBadgeFor(index, navigationBadge),
                )
            }
        }
    }
}

/**
 * Miuix's [NavigationRailItem] only accepts an [ImageVector] icon, so custom bottom bar images
 * need their own item. It mirrors the built-in collapsed/expanded geometry and tinting while
 * swapping the icon painter for [BottomBarIconContent].
 */
@Composable
private fun MiuixCustomNavigationRailItem(
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    iconPath: String,
    label: String,
    badge: (@Composable () -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = MiuixTheme.colorScheme.onSurfaceContainer
    val indicatorColor = MiuixTheme.colorScheme.surfaceContainerHigh
    val shape = RoundedCornerShape(NavigationRailDefaults.ExpandedItemCornerRadius)
    val labelFontSize = if (expanded) {
        NavigationRailDefaults.ExpandedLabelFontSize
    } else {
        NavigationRailDefaults.LabelFontSize
    }
    val iconContent: @Composable () -> Unit = {
        val image: @Composable () -> Unit = {
            BottomBarIconContent(
                icon = icon,
                iconPath = iconPath,
                contentDescription = null,
                tint = tint,
                size = NavigationRailDefaults.IconSize,
            )
        }
        if (badge != null) {
            BadgedBox(badge = { badge() }) { image() }
        } else {
            image()
        }
    }

    if (expanded) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NavigationRailDefaults.ExpandedItemHorizontalMargin)
                .clip(shape)
                .background(if (selected) indicatorColor else Color.Transparent)
                .selectable(
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                )
                .padding(
                    horizontal = NavigationRailDefaults.ExpandedItemContentHorizontalPadding,
                    vertical = NavigationRailDefaults.ExpandedItemContentVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(NavigationRailDefaults.ExpandedItemIconTextSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            iconContent()
            Text(
                text = label,
                color = tint,
                fontSize = labelFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                )
                .padding(vertical = NavigationRailDefaults.ItemVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(if (selected) indicatorColor else Color.Transparent)
                    .padding(
                        horizontal = NavigationRailDefaults.ExpandedItemContentHorizontalPadding,
                        vertical = NavigationRailDefaults.CollapsedIndicatorVerticalPadding,
                    ),
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.height(NavigationRailDefaults.IconTextSpacing))
            Text(
                text = label,
                color = tint,
                fontSize = labelFontSize,
                textAlign = TextAlign.Center,
            )
        }
    }
}
