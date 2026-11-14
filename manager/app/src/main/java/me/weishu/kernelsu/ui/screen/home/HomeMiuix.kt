package me.weishu.kernelsu.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.KernelVersion
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.HomeCardStickerOverlays
import me.weishu.kernelsu.ui.component.WarningLevel
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.miuix.WarningCard
import me.weishu.kernelsu.ui.component.rebootlistpopup.RebootListPopupMiuix
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.backgroundAwareCardColors
import me.weishu.kernelsu.ui.theme.backgroundAwareColor
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.isInDarkTheme
import me.weishu.kernelsu.ui.util.BlurredBar
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.drawPainterCover
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo
import me.weishu.kernelsu.ui.util.rememberBlurBackdrop
import me.weishu.kernelsu.ui.util.shouldShowHomeCardImage
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import me.weishu.kernelsu.ui.component.miuix.AppScaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HomePagerMiuix(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
    onOpenThemeStore: () -> Unit = {},
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val barColor = if (blurActive || hasCustomBackground) Color.Transparent else colorScheme.surface
    AppScaffold(
        containerColor = if (hasCustomBackground) Color.Transparent else colorScheme.surface,
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
                onOpenThemeStore = onOpenThemeStore,
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
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
                item {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (state.checkUpdateEnabled) {
                            UpdateCard(state = state, actions = actions)
                        }
                        if (!state.hideHomeWarnings) {
                            if (state.showManagerPrBuildWarning && state.showFullStatus) {
                                WarningCard(customText(CustomTextKey.HOME_WARN_PR_BUILD), level = WarningLevel.Notice)
                            } else if (state.showKernelPrBuildWarning && state.showFullStatus) {
                                WarningCard(customText(CustomTextKey.HOME_WARN_PR_KERNEL), level = WarningLevel.Notice)
                            }
                            if (state.showGkiWarning && state.showFullStatus) {
                                WarningCard(stringResource(id = R.string.home_gki_warning), level = WarningLevel.Notice)
                            }
                            if (state.requiresNewKernel && state.showFullStatus) {
                                WarningCard(
                                    customText(
                                        if (state.canInstallKernelUpdate) CustomTextKey.HOME_WARN_REQUIRE_KERNEL
                                        else CustomTextKey.HOME_WARN_REQUIRE_KERNEL_GKI
                                    ),
                                    onClick = if (state.canInstallKernelUpdate) actions.onInstallClick else null
                                )
                            }
                            if (state.requiresNewManager) {
                                WarningCard(
                                    customText(CustomTextKey.HOME_WARN_REQUIRE_MANAGER)
                                )
                            }
                            if (state.showLkmUpdate && state.showFullStatus) {
                                WarningCard(
                                    message = customText(CustomTextKey.HOME_WARN_LKM_UPDATE),
                                    level = WarningLevel.Notice,
                                    onClick = actions.onInstallClick,
                                )
                            }
                        }
                        if (state.showRootWarning) {
                            WarningCard(customText(CustomTextKey.HOME_WARN_ROOT_FAILED))
                        }
                        StatusCard(
                            state = state,
                            actions = actions,
                        )
                        InfoCard(
                            systemInfo = state.systemInfo,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SupportLinks(
                            onOpenUrl = actions.onOpenUrl,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(
                            Modifier.height(
                                bottomInnerPadding + if (!Natives.isFullFeatured())
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val newVersion = state.latestVersionInfo
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)
    val updateDialog = rememberConfirmDialog(onConfirm = { actions.onOpenUrl(newVersion.downloadUrl) })

    AnimatedVisibility(
        visible = state.hasUpdate,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        WarningCard(
            message = customText(CustomTextKey.HOME_UPDATE_NEW_VERSION, newVersion.versionCode),
            level = WarningLevel.Notice,
            onClick = {
                if (newVersion.changelog.isEmpty()) {
                    actions.onOpenUrl(newVersion.downloadUrl)
                } else {
                    updateDialog.showConfirm(
                        title = title,
                        content = newVersion.changelog,
                        markdown = true,
                        confirm = updateText
                    )
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
    onOpenThemeStore: () -> Unit = {},
) {
    BlurredBar(backdrop = backdrop, scrollBehavior = scrollBehavior) {
        TopAppBar(
            color = barColor,
            title = customText(CustomTextKey.HOME_TITLE),
            navigationIcon = {
                IconButton(onClick = onOpenThemeStore) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = customText(CustomTextKey.PAGE_THEME_STORE),
                        tint = colorScheme.onBackground,
                    )
                }
            },
            actions = {
                RebootListPopupMiuix()
            },
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val onClick = {
        if (!state.isLateLoadMode) {
            actions.onInstallClick()
        }
    }
    when {
        state.ksuVersion != null && state.homeLayout == HomeLayout.IMMERSIVE -> ImmersiveStatusCardMiuix(
            state = state,
            onClick = onClick,
        )

        state.ksuVersion != null -> ActiveStatusCard(state = state, onClick = onClick)
        else -> InactiveStatusCard(
            state = state,
            actions = actions,
            notInstalled = state.kernelVersion.isGKI(),
            onClick = onClick,
        )
    }
}

@Composable
private fun ActiveStatusCard(
    state: HomeUiState,
    onClick: () -> Unit,
) {
    val isDark = isInDarkTheme()
    val accentColor = if (isDynamicColor) colorScheme.primary else Color(0xFF36D167)
    val tagBackground = when {
        isDynamicColor -> colorScheme.tertiaryContainer
        isDark -> Color(0xFF315D3E)
        else -> Color(0xFFB8E8C5)
    }
    val tagContent = when {
        isDynamicColor -> colorScheme.onTertiaryContainer
        isDark -> Color(0xFFB8E8C5)
        else -> Color(0xFF164A29)
    }
    val workingMode = state.workingModeLabel.takeIf { it.isNotEmpty() }

    val showCardImage = shouldShowHomeCardImage(
        path = state.homeCardImagePath,
        hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty(),
        showWithoutBackground = state.showHomeCardImageWithoutBackground,
    )
    val cardImagePainter = state.homeCardImagePath.takeIf { showCardImage }?.let { path ->
        val file = File(path)
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(file)
                .memoryCacheKey(homeCardImageCacheKey(path))
                .diskCacheKey(homeCardImageCacheKey(path))
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardDefaults.CornerRadius))
            .then(
                if (cardImagePainter != null) {
                    Modifier.drawPainterCover(cardImagePainter)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.defaultColors(
            color = if (cardImagePainter != null) {
                Color.Transparent
            } else {
                backgroundAwareColor(
                    when {
                        isDynamicColor -> colorScheme.secondaryContainer
                        isDark -> Color(0xFF1A3825)
                        else -> Color(0xFFDFFAE4)
                    }
                )
            }
        ),
        onClick = onClick,
        showIndication = !state.isLateLoadMode,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        BoxWithConstraints {
            if (!(state.homeCardImagePath.isNotEmpty() && state.hideHomeCardDecoration)) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(27.dp, 31.dp)
                        .size(150.dp),
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    tint = accentColor.copy(alpha = if (isDynamicColor) 0.8f else 1f),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 188.dp)
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (workingMode != null) {
                        StatusTag(
                            label = workingMode,
                            contentColor = if (isDynamicColor) colorScheme.onPrimary else Color.White,
                            backgroundColor = accentColor,
                        )
                    }
                    if (state.showCustomLkmBadge) {
                        StatusTag(
                            label = customText(CustomTextKey.HOME_TAG_LKM),
                            contentColor = tagContent,
                            backgroundColor = tagBackground,
                        )
                    }
                    if (state.isSafeMode) {
                        StatusTag(
                            label = customText(CustomTextKey.HOME_TAG_SAFEMODE),
                            contentColor = colorScheme.onErrorContainer,
                            backgroundColor = colorScheme.errorContainer,
                        )
                    }
                    if (state.isLateLoadMode) {
                        StatusTag(
                            label = stringResource(id = R.string.jailbreak_mode),
                            contentColor = colorScheme.onErrorContainer,
                            backgroundColor = colorScheme.errorContainer,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!(state.homeCardImagePath.isNotEmpty() && state.hideHomeCardStatusText)) {
                        Text(
                            text = customText(CustomTextKey.HOME_STATUS_WORKING),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(
                                R.string.home_working_version,
                                BuildConfig.VERSION_DISPLAY
                            ),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(
                stickers = state.homeCardStickers,
                cardWidth = maxWidth,
            )
        }
    }
}

@Composable
private fun InactiveStatusCard(
    state: HomeUiState,
    actions: HomeActions,
    notInstalled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = backgroundAwareCardColors(),
        onClick = onClick,
        showIndication = !state.isLateLoadMode,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 188.dp)
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (notInstalled) {
                        customText(CustomTextKey.HOME_STATUS_INSTALL_HINT)
                    } else {
                        stringResource(R.string.home_unsupported_reason)
                    },
                    fontSize = 14.sp,
                    color = colorScheme.error,
                )
            }
            Text(
                text = stringResource(if (notInstalled) R.string.home_not_installed else R.string.home_unsupported),
                modifier = Modifier.padding(vertical = 12.dp),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            if (notInstalled) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        text = customText(CustomTextKey.HOME_BTN_INSTALL),
                        onClick = actions.onInstallClick,
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                    if (state.isSELinuxPermissive) {
                        TextButton(
                            text = stringResource(R.string.home_jailbreak),
                            onClick = actions.onJailbreakClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportLinks(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val learnMoreUrl = stringResource(R.string.home_learn_kernelsu_url)

    Card(modifier = modifier, colors = backgroundAwareCardColors()) {
        ArrowPreference(
            title = stringResource(R.string.home_support_title),
            summary = stringResource(R.string.home_support_content),
            startAction = {
                Icon(
                    imageVector = Icons.Filled.VolunteerActivism,
                    contentDescription = stringResource(R.string.home_support_title),
                    modifier = Modifier.padding(end = 6.dp),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = { onOpenUrl("https://patreon.com/weishu") },
        )
        ArrowPreference(
            title = stringResource(R.string.home_learn_kernelsu),
            summary = stringResource(R.string.home_click_to_learn_kernelsu),
            startAction = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = stringResource(R.string.home_learn_kernelsu),
                    modifier = Modifier.padding(end = 6.dp),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = { onOpenUrl(learnMoreUrl) },
        )
    }
}

@Composable
private fun InfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun InfoText(
        icon: ImageVector,
        title: String,
        content: String,
        bottomPadding: Dp = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp),
                tint = colorScheme.onSurface,
            )
            Column {
                Text(
                    text = title,
                    fontSize = MiuixTheme.textStyles.headline1.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = content,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    val selinuxDisplay = when (systemInfo.selinuxStatus) {
        "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
        "Permissive" -> stringResource(R.string.selinux_status_permissive)
        "Disabled" -> stringResource(R.string.selinux_status_disabled)
        else -> stringResource(R.string.selinux_status_unknown)
    }
    val seccompDisplay = when (systemInfo.seccompStatus) {
        -1 -> stringResource(R.string.seccomp_status_not_supported)
        0 -> stringResource(R.string.seccomp_status_disabled)
        1 -> stringResource(R.string.seccomp_status_strict)
        2 -> stringResource(R.string.seccomp_status_filter)
        else -> stringResource(R.string.seccomp_status_unknown)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth(), colors = backgroundAwareCardColors()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoText(
                    icon = Icons.Filled.Tag,
                    title = customText(CustomTextKey.HOME_INFO_MANAGER_VERSION),
                    content = systemInfo.managerVersion,
                )
                InfoText(
                    icon = Icons.Filled.DeveloperBoard,
                    title = customText(CustomTextKey.HOME_INFO_KERNEL),
                    content = systemInfo.kernelVersion,
                )
                InfoText(
                    icon = Icons.Filled.Smartphone,
                    title = customText(CustomTextKey.HOME_INFO_DEVICE_MODEL),
                    content = systemInfo.deviceModel,
                )
                InfoText(
                    icon = Icons.Filled.Fingerprint,
                    title = customText(CustomTextKey.HOME_INFO_FINGERPRINT),
                    content = systemInfo.fingerprint,
                    bottomPadding = 0.dp,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = backgroundAwareCardColors()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoText(
                    icon = Icons.Filled.Security,
                    title = customText(CustomTextKey.HOME_INFO_SELINUX),
                    content = selinuxDisplay,
                )
                InfoText(
                    icon = Icons.Filled.FilterList,
                    title = customText(CustomTextKey.HOME_INFO_SECCOMP),
                    content = seccompDisplay,
                    bottomPadding = 0.dp,
                )
            }
        }
    }
}

@Preview(name = "Activated")
@Composable
private fun StatusCardActivatedPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true),
        actions = HomeActions({}, {})
    )
}

@Preview(name = "Not Activated")
@Composable
private fun StatusCardNotActivatedPreview() {
    StatusCard(state = previewHomeScreenState(ksuVersion = null, lkmMode = null), actions = HomeActions({}, {}))
}

@Preview(name = "Permissive")
@Composable
private fun StatusCardPermissivePreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive"),
        actions = HomeActions({}, {})
    )
}

@Preview(name = "Jailbreak")
@Composable
private fun StatusCardJailbreakPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true),
        actions = HomeActions({}, {})
    )
}

private val previewSystemInfo = SystemInfo(
    kernelVersion = "6.12.23-android16-5-g123456789000-abogki123456789-4k",
    managerVersion = "3.0.0-30000-abc1234",
    deviceModel = "Xiaomi 17 Pro Max",
    fingerprint = "Xiaomi/popsicle/popsicle:16/BQ2A.250705.001-BP2A.250605.031.A3/OS3.0.313.0.WPBCNXM:user/release-keys",
    selinuxStatus = "Enforcing",
    seccompStatus = 2
)

private val previewUriHandler = object : UriHandler {
    override fun openUri(uri: String) {}
}

@Composable
private fun HomeScreenPreviewContent(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) {
    CompositionLocalProvider(LocalUriHandler provides previewUriHandler) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val actions = HomeActions({}, {})
            StatusCard(
                state = previewHomeScreenState(
                    ksuVersion = ksuVersion,
                    lkmMode = lkmMode,
                    isSafeMode = isSafeMode,
                    isLateLoadMode = isLateLoadMode,
                    selinuxStatus = selinuxStatus,
                ),
                actions = actions
            )
            InfoCard(
                systemInfo = previewSystemInfo.copy(selinuxStatus = selinuxStatus),
                modifier = Modifier.fillMaxWidth(),
            )
            SupportLinks(
                onOpenUrl = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Home Activated", showBackground = true)
@Composable
private fun HomeScreenActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true)
}

@Preview(name = "Home Not Activated", showBackground = true)
@Composable
private fun HomeScreenNotActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null)
}

@Preview(name = "Home Permissive", showBackground = true)
@Composable
private fun HomeScreenPermissivePreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive")
}

@Preview(name = "Home Jailbreak", showBackground = true)
@Composable
private fun HomeScreenJailbreakPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true)
}

private fun previewHomeScreenState(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) = HomeUiState(
    kernelVersion = KernelVersion(6, 1, 0),
    ksuVersion = ksuVersion,
    lkmMode = lkmMode,
    isLkmBundled = lkmMode == true,
    isManager = true,
    isManagerPrBuild = false,
    isKernelPrBuild = false,
    requiresNewKernel = false,
    requiresNewManager = false,
    isRootAvailable = ksuVersion != null,
    isSafeMode = isSafeMode,
    isLateLoadMode = isLateLoadMode,
    checkUpdateEnabled = false,
    latestVersionInfo = LatestVersionInfo(),
    currentManagerVersionCode = 10000,
    systemInfo = previewSystemInfo.copy(selinuxStatus = selinuxStatus),
    kernelUAPIVersion = 1,
    managerUAPIVersion = 1,
)
