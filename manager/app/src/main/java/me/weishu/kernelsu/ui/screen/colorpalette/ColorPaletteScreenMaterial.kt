package me.weishu.kernelsu.ui.screen.colorpalette

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.HomeCardStickerStage
import me.weishu.kernelsu.ui.component.HomeCardStickerThumbnail
import me.weishu.kernelsu.ui.component.bottombar.useNavigationRail
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.ExpressiveToggleButton
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedDropdownItem
import me.weishu.kernelsu.ui.component.material.SegmentedItemContainer
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import me.weishu.kernelsu.ui.component.material.SegmentedSwitchItem
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.component.themeimport.ThemeExportDialog
import me.weishu.kernelsu.ui.component.themeimport.ThemeImportDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState
import me.weishu.kernelsu.ui.screen.settings.ThemePackageOptions
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.customTextGroupsIn
import me.weishu.kernelsu.ui.theme.keyColorOptions
import me.weishu.kernelsu.ui.theme.rememberKernelSUColorScheme
import me.weishu.kernelsu.ui.util.BottomBarIconSlot
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.ThemePackageStorage
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import java.io.File

@Composable
fun ColorPaletteScreenMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val currentKeyColor = uiState.keyColor
    val colorStyle = state.currentPaletteStyle
    val colorSpec = state.currentColorSpec
    val haptic = LocalHapticFeedback.current
    val settingsViewModel = viewModel<SettingsViewModel>()
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showClearAllDialog by rememberSaveable { mutableStateOf(false) }
    var backgroundAppearanceExpanded by rememberSaveable { mutableStateOf(true) }
    var pendingExportOptions by remember { mutableStateOf<ThemePackageOptions?>(null) }
    var selectedCategoryKey by rememberSaveable { mutableStateOf(ThemeSettingsCategory.COLORS.name) }
    val selectedCategory = ThemeSettingsCategory.entries
        .firstOrNull { it.name == selectedCategoryKey }
        ?: ThemeSettingsCategory.COLORS
    val contentListState = rememberLazyListState()
    val pinnedHeader = LocalConfiguration.current.screenHeightDp >= 600
    var categorySelectionSeen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(selectedCategory) {
        if (categorySelectionSeen) {
            // Short screens keep the preview and the category box inside the list.
            contentListState.scrollToItem(if (pinnedHeader) 0 else 2)
        } else {
            categorySelectionSeen = true
        }
    }
    val compactModeLabel = stringResource(
        when {
            currentColorMode.isSystem -> R.string.settings_theme_mode_system
            currentColorMode.isAmoled -> R.string.theme_mode_amoled
            currentColorMode.isDark -> R.string.settings_theme_mode_dark
            else -> R.string.settings_theme_mode_light
        }
    ) + " · " + colorStyle.name
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val previewSpec = ThemePreviewSpec(
        keyColor = currentKeyColor,
        isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme(),
        isAmoled = currentColorMode.isAmoled,
        miuixMonet = uiState.miuixMonet,
        paletteStyle = colorStyle,
        colorSpec = colorSpec,
        backgroundPath = uiState.customBackgroundUri,
        backgroundUpdatedAt = uiState.customBackgroundUpdatedAt,
        cardAlpha = uiState.cardBackgroundAlphaPercent / 100f,
        backgroundBlurPercent = uiState.customBackgroundBlurPercent,
        backgroundScrimPercent = uiState.customBackgroundScrimPercent,
        floatingBottomBar = uiState.enableFloatingBottomBar,
        floatingBottomBarBlur = uiState.enableFloatingBottomBarBlur,
        navigationBadge = uiState.enableNavigationBadge,
        pageScale = uiState.pageScale,
        customHomeTitle = uiState.customHomeTitle,
        homeCardImagePath = uiState.homeCardImagePath,
        hideHomeCardText = uiState.hideHomeCardStatusText,
        hideHomeCardDecoration = uiState.hideHomeCardDecoration,
        homeCardStickers = uiState.homeCardStickers,
        homeLayout = HomeLayout.fromValue(uiState.homeLayout),
        homeStatusAlignment = HomeStatusAlignment.fromValue(uiState.homeStatusAlignment),
        homeStatusScale = uiState.homeStatusScale,
        homeStatusWidthFraction = uiState.homeStatusWidthFraction,
        homePillScale = uiState.homePillScale,
        homeCardImageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle),
        showHomeCardImageWithoutBackground = uiState.showHomeCardImageWithoutBackground,
        appearance = uiState.appearance,
    )

    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                ImageStorage.stageUri(context, uri)?.let { sourcePath ->
                    navigator.push(Route.BackgroundCrop(sourcePath))
                }
            }
        }
    }
    val homeCardImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                ImageStorage.saveHomeCardImage(context, uri)?.let(settingsViewModel::setHomeCardImagePath)
            }
        }
    }
    val homeCardStickerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                ImageStorage.saveHomeCardSticker(context, uri)?.let { path ->
                    settingsViewModel.addHomeCardSticker(HomeCardSticker(path))
                }
            }
        }
    }
    var pendingBottomBarSlot by remember { mutableStateOf<BottomBarIconSlot?>(null) }
    val bottomBarIconPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val slot = pendingBottomBarSlot
        pendingBottomBarSlot = null
        if (uri != null && slot != null) {
            scope.launch {
                ImageStorage.saveBottomBarIcon(context, slot, uri)?.let { path ->
                    settingsViewModel.setBottomBarIcon(slot.key, path)
                }
            }
        }
    }
    val importThemeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(settingsViewModel::importTheme)
    }
    val exportThemeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ThemePackageStorage.THEME_MIME_TYPE)
    ) { uri ->
        val options = pendingExportOptions ?: ThemePackageOptions()
        pendingExportOptions = null
        if (uri != null) {
            settingsViewModel.exportTheme(uri, options)
        }
    }
    val exportImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            val backgroundFile = when {
                uiState.useSystemWallpaper ->
                    ImageStorage.systemWallpaperFile(context).takeIf { it.isFile }
                uiState.customBackgroundUri.isNotBlank() ->
                    File(uiState.customBackgroundUri).takeIf { it.isFile }
                else -> null
            }
            val cardBackgroundFile = uiState.homeCardImagePath
                .takeIf { it.isNotBlank() }
                ?.let { path -> File(path).takeIf { it.isFile } }
            val stickerFiles = uiState.homeCardStickers
                .map { File(it.path) }
                .filter { it.isFile }
            scope.launch {
                ImageStorage.exportImages(
                    context = context,
                    uri = uri,
                    backgroundFile = backgroundFile,
                    cardBackgroundFile = cardBackgroundFile,
                    stickerFiles = stickerFiles,
                ).onSuccess {
                    Toast.makeText(context, R.string.theme_export_success, Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, R.string.theme_export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = {
                    TopBarBackButton(onClick = actions.onBack)
                },
                title = { Text(customText(CustomTextKey.PAGE_APPEARANCE)) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        val navBars = WindowInsets.navigationBars.asPaddingValues()
        val captionBar = WindowInsets.captionBar.asPaddingValues()
        val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
        val isAmoled = currentColorMode.isAmoled

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (pinnedHeader) {
                MaterialCompactPreviewCard(
                    spec = previewSpec,
                    modeLabel = compactModeLabel,
                    onClick = { showFullPreview = true },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                )
                MaterialCategoryGrid(
                    selected = selectedCategory,
                    onSelect = { selectedCategoryKey = it.name },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                )
            }
            LazyColumn(
                state = contentListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                contentPadding = PaddingValues(top = 13.dp),
            ) {
                if (!pinnedHeader) {
                    item {
                        MaterialCompactPreviewCard(
                            spec = previewSpec,
                            modeLabel = compactModeLabel,
                            onClick = { showFullPreview = true },
                        )
                    }
                    item {
                        MaterialCategoryGrid(
                            selected = selectedCategory,
                            onSelect = { selectedCategoryKey = it.name },
                        )
                    }
                }

            if (selectedCategory == ThemeSettingsCategory.COLORS) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Colorize,
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        ColorButtonMaterial(
                            color = Color.Unspecified,
                            isSelected = currentKeyColor == 0,
                            isDark = isDark,
                            isAmoled = isAmoled,
                            paletteStyle = colorStyle,
                            colorSpec = colorSpec,
                            onClick = {
                                actions.onSetKeyColor(0)
                            }
                        )
                    }

                    items(keyColorOptions) { color ->
                        ColorButtonMaterial(
                            color = Color(color),
                            isSelected = currentKeyColor == color,
                            isDark = isDark,
                            isAmoled = isAmoled,
                            paletteStyle = colorStyle,
                            colorSpec = colorSpec,
                            onClick = {
                                actions.onSetKeyColor(color)
                            }
                        )
                    }
                }
            }

            item {
                val options = listOf(
                    listOf(ColorMode.SYSTEM) to stringResource(R.string.settings_theme_mode_system),
                    listOf(ColorMode.LIGHT) to stringResource(R.string.settings_theme_mode_light),
                    listOf(ColorMode.DARK) to stringResource(R.string.settings_theme_mode_dark),
                    listOf(ColorMode.DARK_AMOLED) to stringResource(R.string.settings_theme_mode_dark)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.chunked(4).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            rowOptions.forEachIndexed { index, (modes, label) ->
                                ExpressiveToggleButton(
                                    checked = currentColorMode in modes,
                                    onCheckedChange = {
                                        if (it) {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            actions.onSetColorMode(modes.first())
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { role = Role.RadioButton },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        rowOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                ) {
                                    Icon(
                                        imageVector = when (modes.first()) {
                                            ColorMode.SYSTEM -> Icons.Filled.Brightness4
                                            ColorMode.LIGHT -> Icons.Filled.Brightness7
                                            ColorMode.DARK -> Icons.Filled.Brightness3
                                            ColorMode.DARK_AMOLED -> Icons.Filled.Brightness1
                                            else -> Icons.Filled.Brightness4
                                        },
                                        contentDescription = label
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            val styles = PaletteStyle.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.Style,
                                title = stringResource(R.string.settings_color_style),
                                items = styles.map { it.name },
                                selectedIndex = styles.indexOf(colorStyle),
                                onItemSelected = { index ->
                                    actions.onSetColorStyle(styles[index].name)
                                }
                            )
                        },
                        {
                            val specs = ColorSpec.SpecVersion.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.DesignServices,
                                title = stringResource(R.string.settings_color_spec),
                                items = specs.map { it.name },
                                selectedIndex = specs.indexOf(colorSpec).coerceAtLeast(0),
                                onItemSelected = { index ->
                                    actions.onSetColorSpec(specs[index].name)
                                }
                            )
                        }
                    )
                )
            }
            }

            if (selectedCategory == ThemeSettingsCategory.BOTTOM_BAR) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.CallToAction,
                )
            }
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.BlurOn,
                                title = stringResource(id = R.string.settings_enable_blur),
                                summary = stringResource(id = R.string.settings_enable_blur_summary),
                                checked = uiState.enableBlur,
                                onCheckedChange = actions.onSetEnableBlur
                            )
                        },
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                SegmentedSwitchItem(
                                    icon = Icons.Rounded.BlurOn,
                                    title = stringResource(id = R.string.settings_progressive_blur),
                                    summary = stringResource(id = R.string.settings_progressive_blur_summary),
                                    checked = uiState.enableProgressiveBlur,
                                    onCheckedChange = actions.onSetEnableProgressiveBlur
                                )
                            }
                        },
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.CallToAction,
                                title = stringResource(id = R.string.settings_floating_bottom_bar),
                                summary = stringResource(id = R.string.settings_floating_bottom_bar_summary),
                                checked = uiState.enableFloatingBottomBar,
                                onCheckedChange = actions.onSetEnableFloatingBottomBar
                            )
                        },
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.CallToAction,
                                title = stringResource(id = R.string.settings_enable_glass),
                                summary = stringResource(id = R.string.settings_enable_glass_summary),
                                checked = uiState.enableFloatingBottomBarBlur,
                                onCheckedChange = actions.onSetEnableFloatingBottomBarBlur
                            )
                        },
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Pin,
                                title = stringResource(id = R.string.settings_navigation_badge),
                                summary = stringResource(id = R.string.settings_navigation_badge_summary),
                                checked = uiState.enableNavigationBadge,
                                onCheckedChange = actions.onSetEnableNavigationBadge
                            )
                        }
                    )
                )
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BottomBarIconSlot.entries.forEach { slot ->
                        item(key = slot.key) {
                            val path = uiState.bottomBarIcons[slot.key]?.takeIf { it.isNotBlank() }
                            val label = materialBottomBarSlotLabel(slot)
                            SegmentedListItem(
                                onClick = {
                                    pendingBottomBarSlot = slot
                                    bottomBarIconPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                headlineContent = { Text(label) },
                                supportingContent = {
                                    Text(
                                        stringResource(
                                            if (path != null) {
                                                R.string.settings_bottom_bar_icon_custom_summary
                                            } else {
                                                R.string.settings_bottom_bar_icon_default_summary
                                            }
                                        )
                                    )
                                },
                                leadingContent = {
                                    MaterialBottomBarSlotPreview(
                                        path = path,
                                        icon = materialBottomBarSlotIcon(slot),
                                        contentDescription = label,
                                    )
                                },
                                trailingContent = if (path != null) {
                                    {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    ImageStorage.clearBottomBarIcon(context, slot)
                                                    settingsViewModel.clearBottomBarIcon(slot.key)
                                                }
                                            }
                                        ) {
                                            Text(stringResource(R.string.settings_clear))
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                    if (uiState.bottomBarIcons.isNotEmpty()) {
                        item(key = "clear_all") {
                            SegmentedListItem(
                                onClick = {
                                    scope.launch {
                                        ImageStorage.clearBottomBarIcons(context)
                                        BottomBarIconSlot.entries.forEach { slot ->
                                            settingsViewModel.clearBottomBarIcon(slot.key)
                                        }
                                    }
                                },
                                headlineContent = {
                                    Text(stringResource(R.string.settings_bottom_bar_icons_clear_all))
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.settings_bottom_bar_icons_clear_all_summary))
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.Restore,
                                        stringResource(R.string.settings_bottom_bar_icons_clear_all)
                                    )
                                },
                            )
                        }
                    }
                }
            }
            }

            if (selectedCategory == ThemeSettingsCategory.INTERACTION) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Swipe,
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                item {
                    SegmentedColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        content = listOf(
                            {
                                SegmentedSwitchItem(
                                    icon = Icons.AutoMirrored.Rounded.MenuOpen,
                                    title = stringResource(id = R.string.settings_enable_predictive_back),
                                    summary = stringResource(id = R.string.settings_enable_predictive_back_summary),
                                    checked = uiState.enablePredictiveBack,
                                    onCheckedChange = actions.onSetEnablePredictiveBack
                                )
                            }
                        )
                    )
                }
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Swipe,
                                title = stringResource(id = R.string.settings_enable_swipe_dismiss),
                                summary = stringResource(id = R.string.settings_enable_swipe_dismiss_summary),
                                checked = uiState.enableSwipeDismiss,
                                onCheckedChange = actions.onSetEnableSwipeDismiss,
                            )
                        },
                        {
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.ViewCarousel,
                                title = stringResource(id = R.string.settings_pager_gesture_mode),
                                items = listOf(
                                    stringResource(id = R.string.settings_pager_gesture_native),
                                    stringResource(id = R.string.settings_pager_gesture_cross_axis),
                                    stringResource(id = R.string.settings_pager_gesture_ios_like),
                                ),
                                selectedIndex = uiState.pagerInterceptionMode.coerceIn(0, 2),
                                onItemSelected = actions.onSetPagerInterceptionMode,
                            )
                        },
                    ),
                )
            }

            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val sliderState = rememberSliderState(
                        value = uiState.pageScale,
                        trackRange = 0.8f..1.1f
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.AspectRatio,
                                contentDescription = stringResource(id = R.string.settings_page_scale),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_page_scale),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_page_scale_summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(sliderState.value * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            state = sliderState,
                            onValueChangeFinished = { actions.onSetPageScale(sliderState.value) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            }

            if (selectedCategory == ThemeSettingsCategory.DETAILS) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Description,
                )
            }
            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val sliderState = rememberSliderState(
                        value = uiState.moduleDescriptionMaxLines.toFloat(),
                        steps = 3,
                        trackRange = 1f..5f
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = stringResource(id = R.string.settings_module_description_max_lines),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_module_description_max_lines),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_module_description_max_lines_summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${sliderState.value.roundToInt()} " + stringResource(R.string.unit_lines),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            state = sliderState,
                            onValueChangeFinished = {
                                actions.onSetModuleDescriptionMaxLines(sliderState.value.roundToInt())
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            }

            if (selectedCategory == ThemeSettingsCategory.COLORS) {
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedListItem(
                                onClick = { navigator.push(Route.AppearanceOverlay) },
                                headlineContent = { Text(stringResource(R.string.appearance_overlay_title)) },
                                supportingContent = {
                                    Text(
                                        stringResource(
                                            if (uiState.customAppearanceJson.isNotBlank()) {
                                                R.string.appearance_overlay_active
                                            } else {
                                                R.string.appearance_overlay_summary
                                            }
                                        )
                                    )
                                },
                                leadingContent = { Icon(Icons.Rounded.Palette, stringResource(R.string.appearance_overlay_title)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    )
                )
            }
            }

            if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Home,
                )
            }
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            val customTextsTitle = customText(CustomTextKey.PAGE_CUSTOM_TEXTS)
                            SegmentedListItem(
                                onClick = { navigator.push(Route.CustomTexts) },
                                headlineContent = { Text(customTextsTitle) },
                                supportingContent = { Text(stringResource(R.string.settings_custom_texts_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Edit, customTextsTitle) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    )
                )
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Fingerprint,
                                title = stringResource(R.string.settings_show_fullstatus),
                                summary = stringResource(R.string.settings_show_fullstatus_summary),
                                checked = uiState.showFullStatus,
                                onCheckedChange = settingsViewModel::setShowFullStatus
                            )
                        },
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Warning,
                                title = stringResource(R.string.settings_hide_home_warnings),
                                summary = stringResource(R.string.settings_hide_home_warnings_summary),
                                checked = uiState.hideHomeWarnings,
                                onCheckedChange = settingsViewModel::setHideHomeWarnings
                            )
                        }
                    )
                )
            }
            }
            if (selectedCategory == ThemeSettingsCategory.BACKGROUND) {
            val hasCustomBg = uiState.customBackgroundUri.isNotBlank()
            val usingSystemWallpaper = uiState.useSystemWallpaper
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Wallpaper,
                )
            }
            item {
                val entries: MutableList<@Composable () -> Unit> = mutableListOf()
                if (!usingSystemWallpaper) {
                    entries += {
                        SegmentedListItem(
                            onClick = {
                                backgroundPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.settings_custom_background)) },
                            supportingContent = { Text(stringResource(R.string.settings_custom_background_summary)) },
                            leadingContent = { Icon(Icons.Rounded.Image, stringResource(R.string.settings_custom_background)) },
                            trailingContent = { Icon(Icons.Rounded.Style, null) }
                        )
                    }
                    if (hasCustomBg) {
                        entries += {
                            SegmentedListItem(
                                onClick = {
                                    navigator.push(Route.BackgroundCrop(uiState.customBackgroundUri))
                                },
                                headlineContent = { Text(stringResource(R.string.settings_crop_background)) },
                                supportingContent = { Text(stringResource(R.string.settings_crop_background_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Crop, stringResource(R.string.settings_crop_background)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    }
                }
                entries += {
                    SegmentedSwitchItem(
                        icon = Icons.Rounded.Wallpaper,
                        title = stringResource(R.string.settings_use_system_wallpaper),
                        summary = stringResource(
                            if (uiState.isSystemWallpaperBusy) {
                                R.string.settings_use_system_wallpaper_reading
                            } else {
                                R.string.settings_use_system_wallpaper_summary
                            }
                        ),
                        enabled = !uiState.isSystemWallpaperBusy,
                        checked = uiState.useSystemWallpaper,
                        onCheckedChange = settingsViewModel::setUseSystemWallpaper
                    )
                }
                if (usingSystemWallpaper || hasCustomBg) {
                    entries += {
                        SegmentedSwitchItem(
                            icon = Icons.Rounded.Colorize,
                            title = stringResource(R.string.settings_extract_background_color),
                            summary = stringResource(
                                if (uiState.miuixMonet) {
                                    R.string.settings_extract_background_color_summary
                                } else {
                                    R.string.settings_extract_background_color_disabled
                                }
                            ),
                            checked = uiState.extractColorFromBackground,
                            onCheckedChange = settingsViewModel::setExtractColorFromBackground
                        )
                    }
                }
                val hasCustomBgFile = hasCustomBg && !usingSystemWallpaper
                val hasCardBgFile = uiState.homeCardImagePath.isNotBlank() &&
                    File(uiState.homeCardImagePath).isFile
                val stickerFileCount = uiState.homeCardStickers.count { File(it.path).isFile }
                if (hasCustomBgFile || usingSystemWallpaper || hasCardBgFile || stickerFileCount > 0) {
                    entries += {
                        SegmentedListItem(
                            onClick = {
                                exportImagesLauncher.launch(
                                    ImageStorage.exportImagesFileName(
                                        hasBackground = hasCustomBgFile || usingSystemWallpaper,
                                        hasCardBackground = hasCardBgFile,
                                        stickerCount = stickerFileCount,
                                    )
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.settings_export_images)) },
                            supportingContent = { Text(stringResource(R.string.settings_export_images_summary)) },
                            leadingContent = { Icon(Icons.Rounded.FileDownload, stringResource(R.string.settings_export_images)) },
                            trailingContent = { Icon(Icons.Rounded.Style, null) }
                        )
                    }
                }
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = entries,
                )
            }

            if (usingSystemWallpaper || hasCustomBg) {
            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    item {
                    SegmentedListItem(
                        onClick = { backgroundAppearanceExpanded = !backgroundAppearanceExpanded },
                        headlineContent = { Text(stringResource(R.string.settings_background_appearance)) },
                        supportingContent = { Text(stringResource(R.string.settings_background_appearance_summary)) },
                        leadingContent = { Icon(Icons.Rounded.Tune, stringResource(R.string.settings_background_appearance)) },
                        trailingContent = {
                            Icon(
                                imageVector = if (backgroundAppearanceExpanded) {
                                    Icons.Rounded.KeyboardArrowUp
                                } else {
                                    Icons.Rounded.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    }
                }
            }

            if (backgroundAppearanceExpanded) {
            item {
                MaterialBackgroundSlider(
                    title = stringResource(R.string.settings_card_background_alpha),
                    summary = stringResource(R.string.settings_card_background_alpha_summary),
                    icon = Icons.Rounded.Tune,
                    value = uiState.cardBackgroundAlphaPercent.toFloat(),
                    trackRange = 10f..91f,
                    onValueChangeFinished = settingsViewModel::setCardBackgroundAlphaPercent,
                )
            }
            item {
                MaterialBackgroundSlider(
                    title = stringResource(R.string.settings_background_blur),
                    summary = stringResource(R.string.settings_background_blur_summary),
                    icon = Icons.Rounded.BlurOn,
                    value = uiState.customBackgroundBlurPercent.toFloat(),
                    trackRange = 0f..100f,
                    onValueChangeFinished = settingsViewModel::setCustomBackgroundBlurPercent,
                )
            }
            item {
                MaterialBackgroundSlider(
                    title = stringResource(R.string.settings_background_scrim),
                    summary = stringResource(R.string.settings_background_scrim_summary),
                    icon = Icons.Rounded.Colorize,
                    value = uiState.customBackgroundScrimPercent.toFloat(),
                    trackRange = -100f..100f,
                    onValueChangeFinished = settingsViewModel::setCustomBackgroundScrimPercent,
                    startLabel = stringResource(R.string.settings_background_scrim_darker),
                    endLabel = stringResource(R.string.settings_background_scrim_lighter),
                )
            }
            }
            }
            }

            if (selectedCategory == ThemeSettingsCategory.HOME_CARD) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = stringResource(selectedCategory.summaryRes),
                    icon = Icons.Rounded.Home,
                )
            }
            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val currentLayout = HomeLayout.fromValue(uiState.homeLayout)
                    val layouts = listOf(
                        HomeLayout.CLASSIC to R.string.home_layout_classic,
                        HomeLayout.IMMERSIVE to R.string.home_layout_immersive,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    ) {
                        layouts.forEachIndexed { index, (candidate, labelRes) ->
                            ExpressiveToggleButton(
                                checked = currentLayout == candidate,
                                onCheckedChange = {
                                    if (it) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        settingsViewModel.setHomeLayout(candidate.value)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    layouts.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                            ) {
                                Text(stringResource(labelRes))
                            }
                        }
                    }
                }
            }

            item {
                SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val layout = HomeLayout.fromValue(uiState.homeLayout)
                    val imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle)
                    val cardImagePath = uiState.homeCardImagePath
                    item(key = "card_image") {
                        SegmentedListItem(
                            onClick = {
                                homeCardImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.settings_home_card_image)) },
                            supportingContent = {
                                Text(
                                    stringResource(
                                        if (cardImagePath.isBlank()) {
                                            R.string.settings_home_card_image_default
                                        } else {
                                            R.string.settings_home_card_image_set
                                        }
                                    )
                                )
                            },
                            leadingContent = {
                                MaterialHomeImagePreview(
                                    path = cardImagePath,
                                    contentDescription = stringResource(R.string.settings_home_card_image),
                                )
                            },
                        )
                    }
                    if (cardImagePath.isNotBlank()) {
                        item(key = "card_image_clear") {
                            SegmentedListItem(
                                onClick = {
                                    scope.launch {
                                        ImageStorage.clearHomeCardImage(context)
                                        settingsViewModel.setHomeCardImagePath("")
                                    }
                                },
                                headlineContent = { Text(stringResource(R.string.settings_home_card_image_clear)) },
                                supportingContent = { Text(stringResource(R.string.settings_home_card_image_clear_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Restore, stringResource(R.string.settings_home_card_image_clear)) },
                            )
                        }
                    }
                    if (layout == HomeLayout.IMMERSIVE) {
                        item(key = "image_style") {
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.AspectRatio,
                                title = stringResource(R.string.settings_home_card_image_position),
                                items = listOf(
                                    stringResource(R.string.home_card_image_position_banner),
                                    stringResource(R.string.home_card_image_position_off),
                                ),
                                selectedIndex = if (imageStyle == HomeCardImageStyle.OFF) 1 else 0,
                                onItemSelected = { index ->
                                    settingsViewModel.setHomeCardImageStyle(
                                        if (index == 1) {
                                            HomeCardImageStyle.OFF.value
                                        } else {
                                            HomeCardImageStyle.BANNER.value
                                        }
                                    )
                                },
                            )
                        }
                        if (imageStyle == HomeCardImageStyle.BANNER) {
                            item(key = "image_without_background") {
                                SegmentedSwitchItem(
                                    icon = Icons.Rounded.Wallpaper,
                                    title = stringResource(R.string.settings_show_home_card_image_without_background),
                                    summary = stringResource(R.string.settings_show_home_card_image_without_background_summary),
                                    checked = uiState.showHomeCardImageWithoutBackground,
                                    onCheckedChange = settingsViewModel::setShowHomeCardImageWithoutBackground,
                                )
                            }
                        }
                    }
                    item(key = "hide_status_text") {
                        SegmentedSwitchItem(
                            icon = Icons.Rounded.Edit,
                            title = stringResource(R.string.settings_hide_home_card_status_text),
                            summary = stringResource(R.string.settings_hide_home_card_status_text_summary),
                            checked = uiState.hideHomeCardStatusText,
                            onCheckedChange = settingsViewModel::setHideHomeCardStatusText,
                        )
                    }
                    item(key = "hide_decoration") {
                        SegmentedSwitchItem(
                            icon = Icons.Rounded.Star,
                            title = stringResource(R.string.settings_hide_home_card_decoration),
                            summary = stringResource(R.string.settings_hide_home_card_decoration_summary),
                            checked = uiState.hideHomeCardDecoration,
                            onCheckedChange = settingsViewModel::setHideHomeCardDecoration,
                        )
                    }
                    if (layout == HomeLayout.IMMERSIVE) {
                        item(key = "status_alignment") {
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.AspectRatio,
                                title = stringResource(R.string.settings_home_status_alignment),
                                items = listOf(
                                    stringResource(R.string.home_status_alignment_start),
                                    stringResource(R.string.home_status_alignment_end),
                                ),
                                selectedIndex = if (HomeStatusAlignment.fromValue(uiState.homeStatusAlignment) == HomeStatusAlignment.END) 1 else 0,
                                onItemSelected = { index ->
                                    settingsViewModel.setHomeStatusAlignment(
                                        if (index == 1) {
                                            HomeStatusAlignment.END.value
                                        } else {
                                            HomeStatusAlignment.START.value
                                        }
                                    )
                                },
                            )
                        }
                    }
                    item(key = "home_title") {
                        MaterialHomeTitleRow(
                            value = uiState.customHomeTitle,
                            onValueChange = settingsViewModel::setCustomHomeTitle,
                        )
                    }
                    item(key = "stickers") {
                        MaterialStickerEditor(
                            uiState = uiState,
                            viewModel = settingsViewModel,
                            onPickSticker = {
                                homeCardStickerPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveSticker = { index ->
                                uiState.homeCardStickers.getOrNull(index)?.let { sticker ->
                                    scope.launch {
                                        ImageStorage.deleteHomeCardSticker(context, sticker.path)
                                        settingsViewModel.removeHomeCardSticker(index)
                                    }
                                }
                            },
                        )
                    }
                }
            }

            if (HomeLayout.fromValue(uiState.homeLayout) == HomeLayout.IMMERSIVE) {
                val imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle)
                item {
                    MaterialHomeSlider(
                        title = stringResource(R.string.settings_home_pill_scale),
                        summary = stringResource(R.string.settings_home_pill_scale_summary),
                        icon = Icons.Rounded.Tune,
                        value = uiState.homePillScale,
                        valueRange = HomeStatusStyle.PILL_SCALE_MIN..HomeStatusStyle.PILL_SCALE_MAX,
                        onValueChangeFinished = settingsViewModel::setHomePillScale,
                    )
                }
                if (imageStyle == HomeCardImageStyle.BANNER && uiState.homeCardImagePath.isNotBlank()) {
                    item {
                        MaterialHomeSlider(
                            title = stringResource(R.string.settings_home_status_scale),
                            summary = stringResource(R.string.settings_home_status_scale_summary),
                            icon = Icons.Rounded.AspectRatio,
                            value = uiState.homeStatusScale,
                            valueRange = HomeStatusStyle.SCALE_MIN..HomeStatusStyle.SCALE_MAX,
                            onValueChangeFinished = settingsViewModel::setHomeStatusScale,
                        )
                    }
                    item {
                        MaterialHomeSlider(
                            title = stringResource(R.string.settings_home_status_width),
                            summary = stringResource(R.string.settings_home_status_width_summary),
                            icon = Icons.Rounded.AspectRatio,
                            value = uiState.homeStatusWidthFraction,
                            valueRange = HomeStatusStyle.WIDTH_MIN..HomeStatusStyle.WIDTH_MAX,
                            onValueChangeFinished = settingsViewModel::setHomeStatusWidthFraction,
                        )
                    }
                }
            }
            }

            if (selectedCategory == ThemeSettingsCategory.THEME_PACKAGE) {
            item {
                SegmentedCategoryHeader(
                    title = stringResource(selectedCategory.titleRes),
                    summary = when {
                        uiState.adaptiveDraftComplete ->
                            stringResource(R.string.theme_adaptive_draft_ready)

                        uiState.adaptiveThemeActive && !currentColorMode.isSystem ->
                            stringResource(
                                R.string.theme_adaptive_active_forced,
                                stringResource(
                                    if (currentColorMode.isDark) {
                                        R.string.theme_adaptive_mode_dark
                                    } else {
                                        R.string.theme_adaptive_mode_light
                                    },
                                ),
                            )

                        uiState.adaptiveThemeActive ->
                            stringResource(R.string.theme_adaptive_active)

                        else ->
                            stringResource(selectedCategory.summaryRes)
                    },
                    icon = Icons.Rounded.Palette,
                )
            }
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedListItem(
                                onClick = { importThemeLauncher.launch(arrayOf("*/*")) },
                                headlineContent = { Text(stringResource(R.string.theme_import)) },
                                supportingContent = { Text(stringResource(R.string.theme_import_summary)) },
                                leadingContent = { Icon(Icons.Rounded.FileDownload, stringResource(R.string.theme_import)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        },
                        {
                            SegmentedListItem(
                                onClick = { showExportDialog = true },
                                headlineContent = { Text(stringResource(R.string.theme_export)) },
                                supportingContent = { Text(stringResource(R.string.theme_export_summary)) },
                                leadingContent = { Icon(Icons.Rounded.FileUpload, stringResource(R.string.theme_export)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        },
                        {
                            SegmentedListItem(
                                onClick = { showResetDialog = true },
                                headlineContent = { Text(stringResource(R.string.theme_reset)) },
                                supportingContent = { Text(stringResource(R.string.theme_reset_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Restore, stringResource(R.string.theme_reset)) }
                            )
                        },
                        {
                            SegmentedListItem(
                                onClick = { showClearAllDialog = true },
                                headlineContent = { Text(stringResource(R.string.theme_clear_all)) },
                                supportingContent = { Text(stringResource(R.string.theme_clear_all_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Delete, stringResource(R.string.theme_clear_all)) }
                            )
                        }
                    )
                )
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedListItem(
                                onClick = { navigator.push(Route.AdaptiveThemeBuilder) },
                                headlineContent = { Text(stringResource(R.string.theme_adaptive_builder_title)) },
                                supportingContent = {
                                    Text(
                                        when {
                                            uiState.adaptiveThemeActive && !currentColorMode.isSystem ->
                                                stringResource(
                                                    R.string.theme_adaptive_active_forced,
                                                    stringResource(
                                                        if (currentColorMode.isDark) {
                                                            R.string.theme_adaptive_mode_dark
                                                        } else {
                                                            R.string.theme_adaptive_mode_light
                                                        }
                                                    ),
                                                )

                                            uiState.adaptiveThemeActive ->
                                                stringResource(R.string.theme_adaptive_active)

                                            else ->
                                                stringResource(R.string.theme_adaptive_builder_summary)
                                        }
                                    )
                                },
                                leadingContent = { Icon(Icons.Rounded.AutoAwesome, stringResource(R.string.theme_adaptive_builder_title)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    )
                )
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            val title = customText(CustomTextKey.PAGE_THEME_STORE)
                            SegmentedListItem(
                                onClick = { navigator.push(Route.ThemeStore()) },
                                headlineContent = { Text(title) },
                                supportingContent = { Text(stringResource(R.string.theme_store_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Storefront, title) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        },
                        {
                            SegmentedListItem(
                                onClick = { navigator.push(Route.ThemeStoreSources) },
                                headlineContent = { Text(stringResource(R.string.theme_store_sources)) },
                                supportingContent = { Text(stringResource(R.string.theme_store_sources_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Storefront, stringResource(R.string.theme_store_sources)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        },
                        {
                            SegmentedListItem(
                                onClick = { navigator.push(Route.ThemeAccount()) },
                                headlineContent = { Text(stringResource(R.string.theme_store_account)) },
                                supportingContent = { Text(stringResource(R.string.theme_store_account_summary)) },
                                leadingContent = { Icon(Icons.Rounded.AccountCircle, stringResource(R.string.theme_store_account)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    )
                )
            }
            }

            if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedListItem(
                                onClick = { navigator.push(Route.AppIcon) },
                                headlineContent = { Text(stringResource(R.string.app_icon_title)) },
                                supportingContent = { Text(stringResource(R.string.app_icon_summary)) },
                                leadingContent = { Icon(Icons.Rounded.Apps, stringResource(R.string.app_icon_title)) },
                                trailingContent = { Icon(Icons.Rounded.Style, null) }
                            )
                        }
                    )
                )
            }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp + navBars.calculateBottomPadding() + captionBar.calculateBottomPadding()))
            }
        }
        }

        ThemeImportDialog(
            show = uiState.showThemeImportDialog,
            theme = uiState.pendingImportTheme,
            onDismissRequest = settingsViewModel::dismissImportThemeDialog,
            onConfirm = settingsViewModel::confirmImportTheme,
            forceMaterial = true,
        )
        ThemeExportDialog(
            show = showExportDialog,
            hasBackgroundImage = uiState.customBackgroundUri.isNotBlank() ||
                uiState.systemWallpaperPath.isNotBlank(),
            hasCardImage = uiState.homeCardImagePath.isNotBlank(),
            hasStickers = uiState.homeCardStickers.isNotEmpty(),
            hasAppIcon = uiState.appIconId != "default",
            availableCustomTextGroups = customTextGroupsIn(
                uiState.customTexts,
                uiState.customHomeTitle,
            ),
            onDismissRequest = { showExportDialog = false },
            onConfirm = { options ->
                pendingExportOptions = options
                showExportDialog = false
                exportThemeLauncher.launch(ThemePackageStorage.packageFileName())
            },
            adaptive = uiState.adaptiveThemeActive || uiState.adaptiveDraftComplete,
            hasCustomColors = uiState.customColorsPresent,
            hasImageEffects = uiState.imageEffectsPresent,
            hasBottomBarIcons = uiState.bottomBarIcons.isNotEmpty(),
        )

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(R.string.theme_reset_confirm_title)) },
                text = { Text(stringResource(R.string.theme_reset_confirm_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            settingsViewModel.resetBackgroundAppearance()
                        }
                    ) {
                        Text(stringResource(R.string.theme_reset))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text(stringResource(R.string.theme_clear_all_confirm_title)) },
                text = { Text(stringResource(R.string.theme_clear_all_confirm_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearAllDialog = false
                            settingsViewModel.restoreDefaultTheme()
                        }
                    ) {
                        Text(stringResource(R.string.theme_clear_all_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        ThemeFullscreenPreviewDialog(
            show = showFullPreview,
            onDismiss = { showFullPreview = false },
        ) { previewModifier, page ->
            if (page == 0) {
                ThemePreviewMaterial(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            } else {
                ThemePreviewMiuix(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            }
        }
    }
}

/** Header card that introduces the currently selected theme-settings category. */
@Composable
private fun SegmentedCategoryHeader(
    title: String,
    summary: String,
    icon: ImageVector,
) {
    SegmentedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            SegmentedListItem(
                headlineContent = { Text(title) },
                supportingContent = { Text(summary) },
                leadingContent = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

/**
 * Compact Material preview card pinned above the category list on tall screens. Tapping it opens
 * the full-screen pager so the whole theme can be inspected without leaving the page.
 */
@Composable
private fun MaterialCompactPreviewCard(
    spec: ThemePreviewSpec,
    modeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TonalCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemePreviewMaterial(
                spec = spec,
                modifier = Modifier.width(88.dp),
                compact = true,
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption_material),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = colors.onSurface,
                )
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                ThemePaletteSwatches(
                    colors = listOf(
                        colors.primary,
                        colors.secondaryContainer,
                        colors.tertiaryContainer,
                        colors.surfaceContainerHighest,
                    ),
                )
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** The category grid: only one category's settings are composed at a time. */
@Composable
private fun MaterialCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeSettingsCategory.entries.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { category ->
                        MaterialCategoryTile(
                            category = category,
                            selected = category == selected,
                            onClick = { onSelect(category) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialCategoryTile(
    category: ThemeSettingsCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "ThemeCategoryBackground",
    )
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .selectable(
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
                role = Role.Tab,
            )
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MaterialBackgroundSlider(
    title: String,
    summary: String,
    icon: ImageVector,
    value: Float,
    trackRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Int) -> Unit,
    startLabel: String? = null,
    endLabel: String? = null,
) {
    val sliderState = rememberSliderState(
        value = value,
        trackRange = trackRange
    )
    LaunchedEffect(value) {
        if (sliderState.value.roundToInt() != value.roundToInt()) {
            sliderState.value = value
        }
    }
    TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${sliderState.value.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                state = sliderState,
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState.value.roundToInt())
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (startLabel != null && endLabel != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = startLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = endLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ThemePreviewCard(
    keyColor: Int,
    isDark: Boolean,
    isAmoled: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenRatio = screenWidth / screenHeight
    val useRail = useNavigationRail(enableFloatingBottomBar = false)

    val colorScheme = rememberKernelSUColorScheme(
        seedColor = if (keyColor == 0) Color.Unspecified else Color(keyColor),
        isDark = isDark,
        isAmoled = isAmoled,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(screenRatio),
            color = colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, color = colorScheme.outlineVariant)
        ) {
            val content: @Composable ColumnScope.() -> Unit = {
                // top bar
                Box(
                    modifier = Modifier
                        .height(if (useRail) 36.dp else 48.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, top = if (useRail) 8.dp else 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                    }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val showInfoCard = maxHeight >= 72.dp
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TonalCard(
                            containerColor = colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            content = { }
                        )
                        if (showInfoCard) {
                            TonalCard(
                                containerColor = colorScheme.surfaceBright,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                content = { }
                            )
                        }
                    }
                }
            }

            if (useRail) {
                Row {
                    Surface(
                        color = colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Home, null, tint = colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) { content() }
                }
            } else {
                Column {
                    content()

                    // bottom bar
                    Surface(
                        color = colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Home, null, tint = colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorButtonMaterial(
    color: Color,
    isSelected: Boolean,
    isDark: Boolean,
    isAmoled: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colorScheme = rememberKernelSUColorScheme(
        seedColor = color,
        isDark = isDark,
        isAmoled = isAmoled,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainer,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    color = colorScheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true
                )
                drawArc(
                    color = colorScheme.tertiaryContainer,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }

            val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f)
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(16.dp)
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

/** Slider card with a live percent label; the parent keeps the committed value. */
@Composable
private fun MaterialHomeSlider(
    title: String,
    summary: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Float) -> Unit,
) {
    val sliderState = rememberSliderState(
        value = value,
        trackRange = valueRange
    )
    LaunchedEffect(value) {
        if (sliderState.value != value) {
            sliderState.value = value
        }
    }
    TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${(sliderState.value * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Slider(
                state = sliderState,
                onValueChangeFinished = { onValueChangeFinished(sliderState.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MaterialHomeImagePreview(path: String, contentDescription: String) {
    val file = path.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    } else {
        Icon(
            Icons.Rounded.Image,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Custom home title row + edit dialog. */
@Composable
private fun MaterialHomeTitleRow(value: String, onValueChange: (String) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    SegmentedListItem(
        onClick = { showDialog = true },
        headlineContent = { Text(stringResource(R.string.settings_home_title)) },
        supportingContent = {
            Text(
                value.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.custom_text_default_value, stringResource(R.string.app_name))
            )
        },
        leadingContent = { Icon(Icons.Rounded.Edit, stringResource(R.string.settings_home_title)) },
    )
    if (showDialog) {
        var draft by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_home_title)) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_home_title)) },
                    supportingText = { Text(stringResource(R.string.settings_home_title_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(draft.trim())
                        showDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/** Sticker entry + expandable editor body. */
@Composable
private fun MaterialStickerEditor(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onPickSticker: () -> Unit,
    onRemoveSticker: (Int) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val layout = HomeLayout.fromValue(uiState.homeLayout)
    val imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle)
    val hasBackground = uiState.useSystemWallpaper || uiState.customBackgroundUri.isNotBlank()
    val bannerVisible = imageStyle == HomeCardImageStyle.BANNER &&
        uiState.homeCardImagePath.isNotBlank()
    val canUseStickers = layout == HomeLayout.CLASSIC ||
        (bannerVisible && (hasBackground || uiState.showHomeCardImageWithoutBackground))
    if (!canUseStickers) {
        if (imageStyle == HomeCardImageStyle.BANNER) {
            SegmentedItemContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_home_card_sticker),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            if (bannerVisible && !hasBackground && !uiState.showHomeCardImageWithoutBackground) {
                                R.string.settings_home_card_sticker_hidden
                            } else {
                                R.string.settings_home_card_sticker_banner_only
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }
    val count = uiState.homeCardStickers.size
    val summary = (
        if (count == 0) {
            stringResource(R.string.settings_home_card_sticker_not_set)
        } else {
            stringResource(R.string.settings_home_card_sticker_count, count)
        }
        ) + " · " + stringResource(R.string.settings_home_card_sticker_summary)
    Column {
        SegmentedListItem(
            onClick = { expanded = !expanded },
            headlineContent = { Text(stringResource(R.string.settings_home_card_sticker)) },
            supportingContent = { Text(summary) },
            leadingContent = { Icon(Icons.Rounded.Star, stringResource(R.string.settings_home_card_sticker)) },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        AnimatedVisibility(visible = expanded) {
            MaterialStickerEditorBody(
                uiState = uiState,
                viewModel = viewModel,
                onPickSticker = onPickSticker,
                onRemoveSticker = onRemoveSticker,
            )
        }
    }
}

@Composable
private fun MaterialStickerEditorBody(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onPickSticker: () -> Unit,
    onRemoveSticker: (Int) -> Unit,
) {
    val stickers = uiState.homeCardStickers
    val layout = HomeLayout.fromValue(uiState.homeLayout)
    val immersive = layout == HomeLayout.IMMERSIVE
    var draft by remember { mutableStateOf(stickers) }
    var selected by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(stickers) { draft = stickers }
    LaunchedEffect(draft.size) {
        if (selected > draft.lastIndex) selected = draft.lastIndex.coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeCardStickerStage(
            backgroundImagePath = uiState.homeCardImagePath,
            stickers = draft,
            selectedIndex = selected.coerceIn(0, draft.lastIndex.coerceAtLeast(0)),
            onSelect = { selected = it },
            onTransformChange = { index, transform ->
                draft = draft.mapIndexed { i, sticker ->
                    if (i == index) sticker.copy(transform = transform) else sticker
                }
            },
            onTransformCommit = { index, transform ->
                viewModel.updateHomeCardSticker(index, transform)
            },
            containerColor = if (immersive) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.primary
            },
            contentColor = if (immersive) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
            accentColor = if (immersive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
            accentContentColor = if (immersive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            layout = layout,
            imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle),
            statusScale = uiState.homeStatusScale,
            statusWidthFraction = uiState.homeStatusWidthFraction,
            pillScale = uiState.homePillScale,
            statusAlignment = HomeStatusAlignment.fromValue(uiState.homeStatusAlignment),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            draft.forEachIndexed { index, sticker ->
                HomeCardStickerThumbnail(
                    path = sticker.path,
                    selected = index == selected,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { selected = index },
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (draft.size < HomeCardSticker.MAX_COUNT) {
                TextButton(onClick = onPickSticker) {
                    Text(stringResource(R.string.settings_home_card_sticker_add))
                }
            } else {
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_limit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (draft.isNotEmpty()) {
                TextButton(onClick = { onRemoveSticker(selected.coerceIn(0, draft.lastIndex)) }) {
                    Text(stringResource(R.string.settings_home_card_sticker_remove))
                }
            }
        }
        if (draft.isNotEmpty()) {
            val index = selected.coerceIn(0, draft.lastIndex)
            val transform = draft[index].transform
            val sizeMax = if (immersive) {
                minOf(
                    HomeCardStickerTransform.SIZE_MAX,
                    0.9f * HomeStatusStyle.BannerHeight.value * uiState.homeStatusScale / 360f,
                ).coerceAtLeast(HomeCardStickerTransform.SIZE_MIN)
            } else {
                HomeCardStickerTransform.SIZE_MAX
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_rotation),
                    style = MaterialTheme.typography.titleSmall,
                )
                Slider(
                    value = transform.rotation,
                    onValueChange = { value ->
                        draft = draft.mapIndexed { i, sticker ->
                            if (i == index) {
                                sticker.copy(transform = sticker.transform.copy(rotation = value))
                            } else {
                                sticker
                            }
                        }
                    },
                    onValueChangeFinished = {
                        draft.getOrNull(index)?.let { latest ->
                            val snapped = latest.transform.copy(
                                rotation = latest.transform.rotation.roundToInt().toFloat()
                            )
                            viewModel.updateHomeCardSticker(index, snapped)
                        }
                    },
                    valueRange = HomeCardStickerTransform.ROTATION_MIN..HomeCardStickerTransform.ROTATION_MAX,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${transform.rotation.roundToInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_size),
                    style = MaterialTheme.typography.titleSmall,
                )
                Slider(
                    value = transform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax),
                    onValueChange = { value ->
                        draft = draft.mapIndexed { i, sticker ->
                            if (i == index) {
                                sticker.copy(transform = sticker.transform.copy(size = value))
                            } else {
                                sticker
                            }
                        }
                    },
                    onValueChangeFinished = {
                        draft.getOrNull(index)?.let { latest ->
                            viewModel.updateHomeCardSticker(index, latest.transform)
                        }
                    },
                    valueRange = HomeCardStickerTransform.SIZE_MIN..sizeMax,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${(transform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax) * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MaterialBottomBarSlotPreview(
    path: String?,
    icon: ImageVector,
    contentDescription: String,
) {
    val file = path?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    } else {
        Icon(icon, contentDescription = contentDescription)
    }
}

@Composable
private fun materialBottomBarSlotLabel(slot: BottomBarIconSlot): String = stringResource(
    when (slot) {
        BottomBarIconSlot.Home -> R.string.settings_bottom_bar_icon_home
        BottomBarIconSlot.SuperUser -> R.string.settings_bottom_bar_icon_superuser
        BottomBarIconSlot.Module -> R.string.settings_bottom_bar_icon_module
        BottomBarIconSlot.Setting -> R.string.settings_bottom_bar_icon_setting
    }
)

private fun materialBottomBarSlotIcon(slot: BottomBarIconSlot): ImageVector = when (slot) {
    BottomBarIconSlot.Home -> Icons.Rounded.Wallpaper
    BottomBarIconSlot.SuperUser -> Icons.Rounded.AccountCircle
    BottomBarIconSlot.Module -> Icons.Rounded.Apps
    BottomBarIconSlot.Setting -> Icons.Rounded.Tune
}
