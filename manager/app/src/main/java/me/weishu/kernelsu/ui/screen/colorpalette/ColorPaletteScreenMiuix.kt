package me.weishu.kernelsu.ui.screen.colorpalette

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CallToAction
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
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.HomeCardStickerStage
import me.weishu.kernelsu.ui.component.HomeCardStickerThumbnail
import me.weishu.kernelsu.ui.component.bottombar.useNavigationRail
import me.weishu.kernelsu.ui.component.miuix.ScaleDialog
import me.weishu.kernelsu.ui.component.themeimport.ThemeExportDialog
import me.weishu.kernelsu.ui.component.themeimport.ThemeImportDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState
import me.weishu.kernelsu.ui.screen.settings.ThemePackageOptions
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.customTextGroupsIn
import me.weishu.kernelsu.ui.theme.backgroundAwareColor
import me.weishu.kernelsu.ui.theme.keyColorOptions
import me.weishu.kernelsu.ui.util.BlurredBar
import me.weishu.kernelsu.ui.util.BottomBarIconSlot
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.ThemePackageStorage
import me.weishu.kernelsu.ui.util.rememberBlurBackdrop
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import me.weishu.kernelsu.ui.component.miuix.AppScaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.io.File

@Composable
fun ColorPaletteScreenMiuix(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlurState = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlurState)
    val blurActive = backdrop != null
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val barColor = if (blurActive || hasCustomBackground) Color.Transparent else colorScheme.surface
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
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
            contentListState.scrollToItem(if (pinnedHeader) 0 else 1)
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
    ) + " · " + state.currentPaletteStyle.name
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val previewSpec = ThemePreviewSpec(
        keyColor = uiState.keyColor,
        isDark = isDark,
        isAmoled = currentColorMode.isAmoled,
        miuixMonet = uiState.miuixMonet,
        paletteStyle = state.currentPaletteStyle,
        colorSpec = state.currentColorSpec,
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

    AppScaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = customText(CustomTextKey.PAGE_APPEARANCE),
                    navigationIcon = {
                        IconButton(
                            onClick = actions.onBack
                        ) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val showScaleDialog = rememberSaveable { mutableStateOf(false) }

        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (pinnedHeader) {
                    MiuixCompactPreviewCard(
                        spec = previewSpec,
                        modeLabel = compactModeLabel,
                        onClick = { showFullPreview = true },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                    )
                    MiuixCategoryGrid(
                        selected = selectedCategory,
                        onSelect = { selectedCategoryKey = it.name },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                    )
                }
                LazyColumn(
                    state = contentListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .scrollEndHaptic()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 12.dp),
                    overscrollEffect = null,
                ) {
                    if (!pinnedHeader) {
                        item {
                            MiuixCompactPreviewCard(
                                spec = previewSpec,
                                modeLabel = compactModeLabel,
                                onClick = { showFullPreview = true },
                            )
                            MiuixCategoryGrid(
                                selected = selectedCategory,
                                onSelect = { selectedCategoryKey = it.name },
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            )
                        }
                    }

                if (selectedCategory == ThemeSettingsCategory.COLORS) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                        ) {
                            MiuixCategoryHeader(
                                title = stringResource(selectedCategory.titleRes),
                                summary = stringResource(selectedCategory.summaryRes),
                                icon = Icons.Rounded.Colorize,
                            )
                            Column {
                                val themeItems = listOf(
                                    stringResource(id = R.string.settings_theme_mode_system),
                                    stringResource(id = R.string.settings_theme_mode_light),
                                    stringResource(id = R.string.settings_theme_mode_dark),
                                )
                                TabRowWithContour(
                                    tabs = themeItems,
                                    selectedTabIndex = (if (uiState.themeMode >= 3) uiState.themeMode - 3 else uiState.themeMode)
                                        .coerceIn(0, 2),
                                    onTabSelected = { index -> actions.onSetThemeMode(index) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    colors = TabRowDefaults.tabRowColors(
                                        // The selector sits inside a surface-container card, where the
                                        // default surface track is invisible. Use the highest container
                                        // as the track and the card color as the thumb; both follow the
                                        // shared card opacity so they stay legible over a custom
                                        // background as well.
                                        backgroundColor = backgroundAwareColor(colorScheme.surfaceContainerHighest),
                                        selectedBackgroundColor = backgroundAwareColor(colorScheme.surfaceContainer),
                                    ),
                                )
                                HorizontalDivider()
                                SwitchPreference(
                                    title = stringResource(id = R.string.settings_monet),
                                    startAction = {
                                        Icon(
                                            Icons.Rounded.Wallpaper,
                                            modifier = Modifier.padding(end = 6.dp),
                                            contentDescription = stringResource(id = R.string.settings_monet),
                                            tint = colorScheme.onBackground
                                        )
                                    },
                                    checked = uiState.miuixMonet,
                                    onCheckedChange = {
                                        actions.onSetMiuixMonet(it)
                                    }
                                )

                        AnimatedVisibility(
                            visible = uiState.miuixMonet
                        ) {
                            Column {
                                val colorItems = listOf(
                                    stringResource(id = R.string.settings_key_color_default),
                                    stringResource(id = R.string.color_red),
                                    stringResource(id = R.string.color_pink),
                                    stringResource(id = R.string.color_purple),
                                    stringResource(id = R.string.color_deep_purple),
                                    stringResource(id = R.string.color_indigo),
                                    stringResource(id = R.string.color_blue),
                                    stringResource(id = R.string.color_cyan),
                                    stringResource(id = R.string.color_teal),
                                    stringResource(id = R.string.color_green),
                                    stringResource(id = R.string.color_yellow),
                                    stringResource(id = R.string.color_amber),
                                    stringResource(id = R.string.color_orange),
                                    stringResource(id = R.string.color_brown),
                                    stringResource(id = R.string.color_blue_grey),
                                    stringResource(id = R.string.color_sakura),
                                )
                                val colorValues = listOf(0) + keyColorOptions
                                OverlayDropdownPreference(
                                    title = stringResource(id = R.string.settings_key_color),
                                    items = colorItems,
                                    startAction = {
                                        Icon(
                                            Icons.Rounded.Colorize,
                                            modifier = Modifier.padding(end = 6.dp),
                                            contentDescription = stringResource(id = R.string.settings_key_color),
                                            tint = colorScheme.onBackground
                                        )
                                    },
                                    selectedIndex = colorValues.indexOf(uiState.keyColor).takeIf { it >= 0 } ?: 0,
                                    onSelectedIndexChange = { index ->
                                        actions.onSetKeyColor(colorValues[index])
                                    }
                                )

                                AnimatedVisibility(
                                    visible = uiState.keyColor != 0
                                ) {
                                    Column {
                                        val styles = PaletteStyle.entries
                                        OverlayDropdownPreference(
                                            title = stringResource(R.string.settings_color_style),
                                            startAction = {
                                                Icon(
                                                    Icons.Rounded.Style,
                                                    modifier = Modifier.padding(end = 6.dp),
                                                    contentDescription = stringResource(id = R.string.settings_color_style),
                                                    tint = colorScheme.onBackground
                                                )
                                            },
                                            items = styles.map { it.name },
                                            selectedIndex = styles.indexOfFirst { it.name == uiState.colorStyle }.coerceAtLeast(0),
                                            onSelectedIndexChange = { index ->
                                                actions.onSetColorStyle(styles[index].name)
                                            }
                                        )

                                        val specs = ColorSpec.SpecVersion.entries
                                        OverlayDropdownPreference(
                                            title = stringResource(R.string.settings_color_spec),
                                            startAction = {
                                                Icon(
                                                    Icons.Rounded.DesignServices,
                                                    modifier = Modifier.padding(end = 6.dp),
                                                    contentDescription = stringResource(id = R.string.settings_color_spec),
                                                    tint = colorScheme.onBackground
                                                )
                                            },
                                            items = specs.map { it.name },
                                            selectedIndex = specs.indexOfFirst { it.name == uiState.colorSpec }.coerceAtLeast(0),
                                            onSelectedIndexChange = { index ->
                                                actions.onSetColorSpec(specs[index].name)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                            }
                        }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.BOTTOM_BAR) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
                            title = stringResource(selectedCategory.titleRes),
                            summary = stringResource(selectedCategory.summaryRes),
                            icon = Icons.Rounded.CallToAction,
                        )
                        MiuixBottomBarIconRows(
                            iconPaths = uiState.bottomBarIcons,
                            onPick = { slot ->
                                pendingBottomBarSlot = slot
                                bottomBarIconPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClear = { slot ->
                                scope.launch {
                                    ImageStorage.clearBottomBarIcon(context, slot)
                                    settingsViewModel.clearBottomBarIcon(slot.key)
                                }
                            },
                            onClearAll = {
                                scope.launch {
                                    ImageStorage.clearBottomBarIcons(context)
                                    BottomBarIconSlot.entries.forEach { slot ->
                                        settingsViewModel.clearBottomBarIcon(slot.key)
                                    }
                                }
                            },
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            SwitchPreference(
                                title = stringResource(id = R.string.settings_enable_blur),
                                summary = stringResource(id = R.string.settings_enable_blur_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.BlurOn,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(id = R.string.settings_enable_blur),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = uiState.enableBlur,
                                onCheckedChange = {
                                    actions.onSetEnableBlur(it)
                                }
                            )
                            SwitchPreference(
                                title = stringResource(id = R.string.settings_progressive_blur),
                                summary = stringResource(id = R.string.settings_progressive_blur_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.BlurOn,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(id = R.string.settings_progressive_blur),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = uiState.enableProgressiveBlur,
                                onCheckedChange = {
                                    actions.onSetEnableProgressiveBlur(it)
                                }
                            )
                        }
                        SwitchPreference(
                            title = stringResource(id = R.string.settings_floating_bottom_bar),
                            summary = stringResource(id = R.string.settings_floating_bottom_bar_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.CallToAction,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_floating_bottom_bar),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = uiState.enableFloatingBottomBar,
                            onCheckedChange = {
                                actions.onSetEnableFloatingBottomBar(it)
                            }
                        )
                        AnimatedVisibility(visible = uiState.enableFloatingBottomBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            SwitchPreference(
                                title = stringResource(id = R.string.settings_enable_glass),
                                summary = stringResource(id = R.string.settings_enable_glass_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.WaterDrop,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(id = R.string.settings_enable_glass),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = uiState.enableFloatingBottomBarBlur,
                                onCheckedChange = {
                                    actions.onSetEnableFloatingBottomBarBlur(it)
                                }
                            )
                        }
                        SwitchPreference(
                            title = stringResource(id = R.string.settings_navigation_badge),
                            summary = stringResource(id = R.string.settings_navigation_badge_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Pin,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_navigation_badge),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = uiState.enableNavigationBadge,
                            onCheckedChange = {
                                actions.onSetEnableNavigationBadge(it)
                            }
                        )
                    }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.INTERACTION) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
                            title = stringResource(selectedCategory.titleRes),
                            summary = stringResource(selectedCategory.summaryRes),
                            icon = Icons.Rounded.Swipe,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            SwitchPreference(
                                title = stringResource(id = R.string.settings_enable_predictive_back),
                                summary = stringResource(id = R.string.settings_enable_predictive_back_summary),
                                startAction = {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.MenuOpen,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(id = R.string.settings_enable_predictive_back),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = uiState.enablePredictiveBack,
                                onCheckedChange = {
                                    actions.onSetEnablePredictiveBack(it)
                                }
                            )
                        }
                        SwitchPreference(
                            title = stringResource(id = R.string.settings_enable_swipe_dismiss),
                            summary = stringResource(id = R.string.settings_enable_swipe_dismiss_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Swipe,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_enable_swipe_dismiss),
                                    tint = colorScheme.onBackground,
                                )
                            },
                            checked = uiState.enableSwipeDismiss,
                            onCheckedChange = actions.onSetEnableSwipeDismiss,
                        )
                        OverlayDropdownPreference(
                            title = stringResource(id = R.string.settings_pager_gesture_mode),
                            items = listOf(
                                stringResource(id = R.string.settings_pager_gesture_native),
                                stringResource(id = R.string.settings_pager_gesture_cross_axis),
                                stringResource(id = R.string.settings_pager_gesture_ios_like),
                            ),
                            startAction = {
                                Icon(
                                    Icons.Rounded.ViewCarousel,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_pager_gesture_mode),
                                    tint = colorScheme.onBackground,
                                )
                            },
                            selectedIndex = uiState.pagerInterceptionMode.coerceIn(0, 2),
                            onSelectedIndexChange = actions.onSetPagerInterceptionMode,
                        )

                        var sliderValue by remember(uiState.pageScale) { mutableFloatStateOf(uiState.pageScale) }
                        ArrowPreference(
                            title = stringResource(id = R.string.settings_page_scale),
                            summary = stringResource(id = R.string.settings_page_scale_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.AspectRatio,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_page_scale),
                                    tint = colorScheme.onBackground
                                )
                            },
                            endActions = {
                                Text(
                                    text = "${(sliderValue * 100).toInt()}%",
                                    color = colorScheme.onSurfaceVariantActions,
                                )
                            },
                            onClick = { showScaleDialog.value = !showScaleDialog.value },
                            holdDownState = showScaleDialog.value,
                            bottomAction = {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        sliderValue = it
                                    },
                                    onValueChangeFinished = {
                                        actions.onSetPageScale(sliderValue)
                                    },
                                    valueRange = 0.8f..1.1f,
                                    showKeyPoints = true,
                                    keyPoints = listOf(0.8f, 0.9f, 1f, 1.1f),
                                    magnetThreshold = 0.01f,
                                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                                )
                            },
                        )
                        ScaleDialog(
                            show = showScaleDialog.value,
                            onDismissRequest = { showScaleDialog.value = false },
                            volumeState = { uiState.pageScale },
                            onVolumeChange = {
                                actions.onSetPageScale(it)
                            }
                        )
                    }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.DETAILS) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
                            title = stringResource(selectedCategory.titleRes),
                            summary = stringResource(selectedCategory.summaryRes),
                            icon = Icons.Rounded.Description,
                        )
                        var linesValue by remember(uiState.moduleDescriptionMaxLines) { mutableIntStateOf(uiState.moduleDescriptionMaxLines) }
                        BasicComponent(
                            title = stringResource(id = R.string.settings_module_description_max_lines),
                            summary = stringResource(id = R.string.settings_module_description_max_lines_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Description,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(id = R.string.settings_module_description_max_lines),
                                    tint = colorScheme.onBackground
                                )
                            },
                            endActions = {
                                Text(
                                    text = "$linesValue " + stringResource(R.string.unit_lines),
                                    color = colorScheme.onSurfaceVariantActions,
                                )
                            },
                            bottomAction = {
                                Slider(
                                    value = linesValue.toFloat(),
                                    onValueChange = {
                                        linesValue = it.roundToInt()
                                    },
                                    onValueChangeFinished = {
                                        actions.onSetModuleDescriptionMaxLines(linesValue)
                                    },
                                    valueRange = 1f..5f,
                                    showKeyPoints = true,
                                    keyPoints = listOf(1f, 2f, 3f, 4f, 5f),
                                    magnetThreshold = 0.25f,
                                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                                )
                            },
                        )
                    }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.COLORS) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                        ) {
                            ArrowPreference(
                                title = stringResource(R.string.appearance_overlay_title),
                                summary = stringResource(
                                    if (uiState.customAppearanceJson.isNotBlank()) {
                                        R.string.appearance_overlay_active
                                    } else {
                                        R.string.appearance_overlay_summary
                                    }
                                ),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.Palette,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(R.string.appearance_overlay_title),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                onClick = { navigator.push(Route.AppearanceOverlay) }
                            )
                        }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                        ) {
                            MiuixCategoryHeader(
                                title = stringResource(selectedCategory.titleRes),
                                summary = stringResource(selectedCategory.summaryRes),
                                icon = Icons.Rounded.Home,
                            )
                            ArrowPreference(
                                title = customText(CustomTextKey.PAGE_CUSTOM_TEXTS),
                                summary = stringResource(R.string.settings_custom_texts_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = customText(CustomTextKey.PAGE_CUSTOM_TEXTS),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                onClick = { navigator.push(Route.CustomTexts) }
                            )
                        }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.BACKGROUND) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
                            title = stringResource(selectedCategory.titleRes),
                            summary = stringResource(selectedCategory.summaryRes),
                            icon = Icons.Rounded.Wallpaper,
                        )
                        val hasCustomBg = uiState.customBackgroundUri.isNotBlank()
                        val usingSystemWallpaper = uiState.useSystemWallpaper
                        if (!usingSystemWallpaper) {
                        ArrowPreference(
                            title = stringResource(R.string.settings_custom_background),
                            summary = stringResource(R.string.settings_custom_background_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Image,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.settings_custom_background),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = {
                                backgroundPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        }
                        if (hasCustomBg && !usingSystemWallpaper) {
                            ArrowPreference(
                                title = stringResource(R.string.settings_crop_background),
                                summary = stringResource(R.string.settings_crop_background_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.Crop,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(R.string.settings_crop_background),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                onClick = { navigator.push(Route.BackgroundCrop(uiState.customBackgroundUri)) }
                            )
                        }
                        SwitchPreference(
                            title = stringResource(R.string.settings_use_system_wallpaper),
                            summary = stringResource(
                                if (uiState.isSystemWallpaperBusy) {
                                    R.string.settings_use_system_wallpaper_reading
                                } else {
                                    R.string.settings_use_system_wallpaper_summary
                                }
                            ),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Wallpaper,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.settings_use_system_wallpaper),
                                    tint = colorScheme.onBackground
                                )
                            },
                            enabled = !uiState.isSystemWallpaperBusy,
                            checked = uiState.useSystemWallpaper,
                            onCheckedChange = settingsViewModel::setUseSystemWallpaper
                        )
                        if (usingSystemWallpaper || hasCustomBg) {
                            SwitchPreference(
                                title = stringResource(R.string.settings_extract_background_color),
                                summary = stringResource(
                                    if (uiState.miuixMonet) {
                                        R.string.settings_extract_background_color_summary
                                    } else {
                                        R.string.settings_extract_background_color_disabled
                                    }
                                ),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.Colorize,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(R.string.settings_extract_background_color),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = uiState.extractColorFromBackground,
                                onCheckedChange = settingsViewModel::setExtractColorFromBackground
                            )
                        }

                        val hasCustomBgFile = hasCustomBg && !usingSystemWallpaper
                        val hasCardBgFile = uiState.homeCardImagePath.isNotBlank() &&
                            File(uiState.homeCardImagePath).isFile
                        val stickerFileCount = uiState.homeCardStickers.count { File(it.path).isFile }
                        if (hasCustomBgFile || usingSystemWallpaper || hasCardBgFile || stickerFileCount > 0) {
                            ArrowPreference(
                                title = stringResource(R.string.settings_export_images),
                                summary = stringResource(R.string.settings_export_images_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.FileDownload,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(R.string.settings_export_images),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                onClick = {
                                    exportImagesLauncher.launch(
                                        ImageStorage.exportImagesFileName(
                                            hasBackground = hasCustomBgFile || usingSystemWallpaper,
                                            hasCardBackground = hasCardBgFile,
                                            stickerCount = stickerFileCount,
                                        )
                                    )
                                }
                            )
                        }

                        if (usingSystemWallpaper || hasCustomBg) {
                            BasicComponent(
                                title = stringResource(R.string.settings_background_appearance),
                                summary = stringResource(R.string.settings_background_appearance_summary),
                                startAction = {
                                    Icon(
                                        Icons.Rounded.Tune,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = stringResource(R.string.settings_background_appearance),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                endActions = {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = if (backgroundAppearanceExpanded) 180f else 0f
                                        },
                                        tint = colorScheme.onBackground,
                                    )
                                },
                                onClick = {
                                    backgroundAppearanceExpanded = !backgroundAppearanceExpanded
                                },
                            )
                            AnimatedVisibility(visible = backgroundAppearanceExpanded) {
                                Column {
                                    MiuixBackgroundSlider(
                                        title = stringResource(R.string.settings_card_background_alpha),
                                        summary = stringResource(R.string.settings_card_background_alpha_summary),
                                        icon = Icons.Rounded.Tune,
                                        value = uiState.cardBackgroundAlphaPercent.toFloat(),
                                        valueRange = 10f..91f,
                                        onValueChangeFinished = settingsViewModel::setCardBackgroundAlphaPercent,
                                    )
                                    MiuixBackgroundSlider(
                                        title = stringResource(R.string.settings_background_blur),
                                        summary = stringResource(R.string.settings_background_blur_summary),
                                        icon = Icons.Rounded.BlurOn,
                                        value = uiState.customBackgroundBlurPercent.toFloat(),
                                        valueRange = 0f..100f,
                                        onValueChangeFinished = settingsViewModel::setCustomBackgroundBlurPercent,
                                    )
                                    MiuixBackgroundSlider(
                                        title = stringResource(R.string.settings_background_scrim),
                                        summary = stringResource(R.string.settings_background_scrim_summary),
                                        icon = Icons.Rounded.Colorize,
                                        value = uiState.customBackgroundScrimPercent.toFloat(),
                                        valueRange = -100f..100f,
                                        onValueChangeFinished = settingsViewModel::setCustomBackgroundScrimPercent,
                                        startLabel = stringResource(R.string.settings_background_scrim_darker),
                                        endLabel = stringResource(R.string.settings_background_scrim_lighter),
                                    )
                                }
                            }
                        }
                    }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.HOME_CARD) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
                            title = stringResource(selectedCategory.titleRes),
                            summary = stringResource(selectedCategory.summaryRes),
                            icon = Icons.Rounded.Home,
                        )
                        HomeCardSectionMiuix(
                            uiState = uiState,
                            viewModel = settingsViewModel,
                            onPickImage = {
                                homeCardImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickSticker = {
                                homeCardStickerPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearImage = {
                                scope.launch {
                                    ImageStorage.clearHomeCardImage(context)
                                    settingsViewModel.setHomeCardImagePath("")
                                }
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

                if (selectedCategory == ThemeSettingsCategory.THEME_PACKAGE) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        MiuixCategoryHeader(
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
                        BasicComponent(
                            title = stringResource(R.string.theme_import),
                            summary = stringResource(R.string.theme_import_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.FileDownload,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_import),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { importThemeLauncher.launch(arrayOf("*/*")) }
                        )
                        BasicComponent(
                            title = stringResource(R.string.theme_export),
                            summary = stringResource(R.string.theme_export_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.FileUpload,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_export),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { showExportDialog = true }
                        )
                        BasicComponent(
                            title = stringResource(R.string.theme_reset),
                            summary = stringResource(R.string.theme_reset_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Restore,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_reset),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { showResetDialog = true }
                        )
                        BasicComponent(
                            title = stringResource(R.string.theme_clear_all),
                            summary = stringResource(R.string.theme_clear_all_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_clear_all),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { showClearAllDialog = true }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        ArrowPreference(
                            title = stringResource(R.string.theme_adaptive_builder_title),
                            summary = when {
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
                            },
                            startAction = {
                                Icon(
                                    Icons.Rounded.AutoAwesome,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_adaptive_builder_title),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { navigator.push(Route.AdaptiveThemeBuilder) }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        ArrowPreference(
                            title = customText(CustomTextKey.PAGE_THEME_STORE),
                            summary = stringResource(R.string.theme_store_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Storefront,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = customText(CustomTextKey.PAGE_THEME_STORE),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { navigator.push(Route.ThemeStore()) }
                        )
                        ArrowPreference(
                            title = stringResource(R.string.theme_store_sources),
                            summary = stringResource(R.string.theme_store_sources_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Storefront,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_store_sources),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { navigator.push(Route.ThemeStoreSources) }
                        )
                        ArrowPreference(
                            title = stringResource(R.string.theme_store_account),
                            summary = stringResource(R.string.theme_store_account_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.AccountCircle,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.theme_store_account),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { navigator.push(Route.ThemeAccount()) }
                        )
                    }
                    }
                }

                if (selectedCategory == ThemeSettingsCategory.ICON_TEXT) {
                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        ArrowPreference(
                            title = stringResource(R.string.app_icon_title),
                            summary = stringResource(R.string.app_icon_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Apps,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.app_icon_title),
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = { navigator.push(Route.AppIcon) }
                        )
                    }
                    }

                    item {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        SwitchPreference(
                            title = stringResource(R.string.settings_show_fullstatus),
                            summary = stringResource(R.string.settings_show_fullstatus_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Fingerprint,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.settings_show_fullstatus),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = uiState.showFullStatus,
                            onCheckedChange = settingsViewModel::setShowFullStatus
                        )
                        SwitchPreference(
                            title = stringResource(R.string.settings_hide_home_warnings),
                            summary = stringResource(R.string.settings_hide_home_warnings_summary),
                            startAction = {
                                Icon(
                                    Icons.Rounded.Warning,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = stringResource(R.string.settings_hide_home_warnings),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = uiState.hideHomeWarnings,
                            onCheckedChange = settingsViewModel::setHideHomeWarnings
                        )
                    }
                    }
                }
                item {
                    Spacer(
                        Modifier.height(
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                    WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() +
                                    12.dp
                        )
                    )
                }
            }
        }
        }

        ThemeImportDialog(
            show = uiState.showThemeImportDialog,
            theme = uiState.pendingImportTheme,
            onDismissRequest = settingsViewModel::dismissImportThemeDialog,
            onConfirm = settingsViewModel::confirmImportTheme,
            forceMaterial = false,
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
            OverlayDialog(
                show = true,
                title = stringResource(R.string.theme_reset_confirm_title),
                summary = stringResource(R.string.theme_reset_confirm_message),
                onDismissRequest = { showResetDialog = false },
                content = {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(
                            text = stringResource(R.string.cancel),
                            onClick = { showResetDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            text = stringResource(R.string.theme_reset),
                            onClick = {
                                showResetDialog = false
                                settingsViewModel.resetBackgroundAppearance()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                },
            )
        }

        if (showClearAllDialog) {
            OverlayDialog(
                show = true,
                title = stringResource(R.string.theme_clear_all_confirm_title),
                summary = stringResource(R.string.theme_clear_all_confirm_message),
                onDismissRequest = { showClearAllDialog = false },
                content = {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(
                            text = stringResource(R.string.cancel),
                            onClick = { showClearAllDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            text = stringResource(R.string.theme_clear_all_confirm),
                            onClick = {
                                showClearAllDialog = false
                                settingsViewModel.restoreDefaultTheme()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                },
            )
        }

        ThemeFullscreenPreviewDialog(
            show = showFullPreview,
            onDismiss = { showFullPreview = false },
        ) { previewModifier, page ->
            if (page == 0) {
                ThemePreviewMiuix(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            } else {
                ThemePreviewMaterial(
                    spec = previewSpec,
                    modifier = previewModifier,
                    expanded = true,
                )
            }
        }
    }
}

/**
 * Compact preview card pinned above the category list on tall screens. Tapping it opens the
 * full-screen Miuix/Material pager so the whole theme can be inspected without leaving the page.
 */
@Composable
private fun MiuixCompactPreviewCard(
    spec: ThemePreviewSpec,
    modeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(0.dp),
        onClick = onClick,
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemePreviewMiuix(
                spec = spec,
                modifier = Modifier.width(88.dp),
                compact = true,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption_miuix),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = modeLabel,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                ThemePaletteSwatches(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.secondaryContainer,
                        colorScheme.tertiaryContainer,
                        colorScheme.surfaceContainerHighest,
                    ),
                )
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun MiuixCategoryHeader(
    title: String,
    summary: String,
    icon: ImageVector,
) {
    BasicComponent(
        title = title,
        summary = summary,
        startAction = {
            Icon(
                imageVector = icon,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = title,
                tint = colorScheme.onBackground,
            )
        },
    )
}

/** The category grid: only one category's settings are composed at a time. */
@Composable
private fun MiuixCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeSettingsCategory.entries.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { category ->
                        MiuixCategoryTile(
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
private fun MiuixCategoryTile(
    category: ThemeSettingsCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = backgroundAwareColor(
            if (selected) colorScheme.primary else colorScheme.surfaceContainerHighest
        ),
        label = "ThemeCategoryBackground",
    )
    val contentColor = if (selected) colorScheme.onPrimary else colorScheme.onSurface
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .height(48.dp)
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
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MiuixBackgroundSlider(
    title: String,
    summary: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Int) -> Unit,
    startLabel: String? = null,
    endLabel: String? = null,
) {
    var draft by remember(value) { mutableFloatStateOf(value) }
    BasicComponent(
        title = title,
        summary = summary,
        startAction = {
            Icon(
                icon,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = title,
                tint = colorScheme.onBackground
            )
        },
        endActions = {
            Text(
                text = "${draft.roundToInt()}%",
                color = colorScheme.onSurfaceVariantActions,
            )
        },
        bottomAction = {
            Column {
                Slider(
                    value = draft,
                    onValueChange = { draft = it },
                    onValueChangeFinished = {
                        val percent = draft.roundToInt()
                        draft = percent.toFloat()
                        onValueChangeFinished(percent)
                    },
                    valueRange = valueRange,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                )
                if (startLabel != null && endLabel != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = startLabel,
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = endLabel,
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        },
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ThemePreviewCardMiuix(
    keyColor: Int,
    isDark: Boolean,
    miuixMonet: Boolean,
    enableFloatingBottomBar: Boolean = false,
    enableFloatingBottomBarBlur: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2021,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenRatio = screenWidth / screenHeight
    val useRail = useNavigationRail(enableFloatingBottomBar)

    val seedColor = if (keyColor == 0) colorScheme.primary else Color(keyColor)
    val effectiveStyle = if (keyColor == 0) PaletteStyle.TonalSpot else paletteStyle
    val effectiveSpec = if (keyColor == 0) ColorSpec.SpecVersion.Default else colorSpec
    val dynamicCs = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        style = effectiveStyle,
        specVersion = effectiveSpec,
    )

    val bgColor = if (miuixMonet) dynamicCs.background else colorScheme.surface
    val textColor = if (miuixMonet) dynamicCs.onSurface else colorScheme.onBackground
    val accentCardColor = when {
        miuixMonet -> dynamicCs.secondaryContainer
        isDark -> Color(0xFF1A3825)
        else -> Color(0xFFDFFAE4)
    }
    val cardColor = if (miuixMonet) dynamicCs.surfaceContainerHighest else colorScheme.surfaceVariant
    val navBarColor = if (miuixMonet) dynamicCs.surfaceContainer else colorScheme.surface
    val iconColor = if (miuixMonet) dynamicCs.primary else colorScheme.primary
    val navSelectedColor = colorScheme.onSurfaceContainer
    val navUnselectedColor = colorScheme.onSurfaceContainer.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(screenRatio)
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(1.dp, colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            val content = @Composable {
                Column {
                    Row(
                        modifier = Modifier
                            .height(if (useRail) 36.dp else 48.dp)
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = if (useRail) 12.dp else 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentCardColor)
                    )

                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        val smallCardHeight = 12.dp
                        val smallCardCount = when {
                            maxHeight >= 96.dp -> 2
                            maxHeight >= 72.dp -> 1
                            else -> 0
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cardColor)
                            )
                            repeat(smallCardCount) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(smallCardHeight)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cardColor)
                                )
                            }
                        }
                    }
                }
            }

            if (useRail) {
                Row {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(30.dp)
                            .background(navBarColor),
                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (it == 0) navSelectedColor else navUnselectedColor)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(0.5.dp)
                            .background(textColor.copy(alpha = 0.1f))
                    )
                    Box(modifier = Modifier.weight(1f)) { content() }
                }
            } else {
                content()
            }

            if (!useRail && enableFloatingBottomBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (enableFloatingBottomBarBlur) navBarColor.copy(alpha = 0.5f)
                                else navBarColor
                            )
                            .border(0.5.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (it == 0) iconColor else textColor)
                            )
                        }
                    }
                }
            } else if (!useRail) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(textColor.copy(alpha = 0.1f))
                    )
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .fillMaxWidth()
                            .background(navBarColor)
                            .padding(top = 2.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (it == 0) navSelectedColor else navUnselectedColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** HOME CARD: layout, card image, display switches, immersive sliders, title and stickers. */
@Composable
private fun HomeCardSectionMiuix(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onPickImage: () -> Unit,
    onPickSticker: () -> Unit,
    onClearImage: () -> Unit,
    onRemoveSticker: (Int) -> Unit,
) {
    val layout = HomeLayout.fromValue(uiState.homeLayout)
    val imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle)
    val cardImagePath = uiState.homeCardImagePath

    BasicComponent(
        title = stringResource(R.string.settings_section_home_card),
        summary = stringResource(R.string.settings_section_home_card_summary),
        startAction = {
            Icon(
                Icons.Rounded.Wallpaper,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_section_home_card),
                tint = colorScheme.onBackground
            )
        },
    )

    TabRow(
        tabs = listOf(
            stringResource(R.string.home_layout_classic),
            stringResource(R.string.home_layout_immersive),
        ),
        selectedTabIndex = if (layout == HomeLayout.IMMERSIVE) 1 else 0,
        onTabSelected = { index ->
            viewModel.setHomeLayout(
                if (index == 1) HomeLayout.IMMERSIVE.value else HomeLayout.CLASSIC.value
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )

    ArrowPreference(
        title = stringResource(R.string.settings_home_card_image),
        summary = stringResource(
            if (cardImagePath.isBlank()) {
                R.string.settings_home_card_image_default
            } else {
                R.string.settings_home_card_image_set
            }
        ),
        startAction = {
            MiuixHomeImagePreview(
                path = cardImagePath,
                contentDescription = stringResource(R.string.settings_home_card_image),
            )
        },
        onClick = onPickImage,
    )

    if (cardImagePath.isNotBlank()) {
        ArrowPreference(
            title = stringResource(R.string.settings_home_card_image_clear),
            summary = stringResource(R.string.settings_home_card_image_clear_summary),
            startAction = {
                Icon(
                    Icons.Rounded.Restore,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(R.string.settings_home_card_image_clear),
                    tint = colorScheme.onBackground
                )
            },
            onClick = onClearImage,
        )
    }

    if (layout == HomeLayout.IMMERSIVE) {
        OverlayDropdownPreference(
            title = stringResource(R.string.settings_home_card_image_position),
            items = listOf(
                stringResource(R.string.home_card_image_position_banner),
                stringResource(R.string.home_card_image_position_off),
            ),
            startAction = {
                Icon(
                    Icons.Rounded.AspectRatio,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(R.string.settings_home_card_image_position),
                    tint = colorScheme.onBackground
                )
            },
            selectedIndex = if (imageStyle == HomeCardImageStyle.OFF) 1 else 0,
            onSelectedIndexChange = { index ->
                viewModel.setHomeCardImageStyle(
                    if (index == 1) HomeCardImageStyle.OFF.value else HomeCardImageStyle.BANNER.value
                )
            },
        )
        if (imageStyle == HomeCardImageStyle.BANNER) {
            SwitchPreference(
                title = stringResource(R.string.settings_show_home_card_image_without_background),
                summary = stringResource(R.string.settings_show_home_card_image_without_background_summary),
                startAction = {
                    Icon(
                        Icons.Rounded.Wallpaper,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = stringResource(R.string.settings_show_home_card_image_without_background),
                        tint = colorScheme.onBackground
                    )
                },
                checked = uiState.showHomeCardImageWithoutBackground,
                onCheckedChange = viewModel::setShowHomeCardImageWithoutBackground,
            )
        }
    }

    SwitchPreference(
        title = stringResource(R.string.settings_hide_home_card_status_text),
        summary = stringResource(R.string.settings_hide_home_card_status_text_summary),
        startAction = {
            Icon(
                Icons.Rounded.Edit,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_hide_home_card_status_text),
                tint = colorScheme.onBackground
            )
        },
        checked = uiState.hideHomeCardStatusText,
        onCheckedChange = viewModel::setHideHomeCardStatusText,
    )
    SwitchPreference(
        title = stringResource(R.string.settings_hide_home_card_decoration),
        summary = stringResource(R.string.settings_hide_home_card_decoration_summary),
        startAction = {
            Icon(
                Icons.Rounded.Star,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_hide_home_card_decoration),
                tint = colorScheme.onBackground
            )
        },
        checked = uiState.hideHomeCardDecoration,
        onCheckedChange = viewModel::setHideHomeCardDecoration,
    )

    if (layout == HomeLayout.IMMERSIVE) {
        OverlayDropdownPreference(
            title = stringResource(R.string.settings_home_status_alignment),
            items = listOf(
                stringResource(R.string.home_status_alignment_start),
                stringResource(R.string.home_status_alignment_end),
            ),
            startAction = {
                Icon(
                    Icons.Rounded.AspectRatio,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(R.string.settings_home_status_alignment),
                    tint = colorScheme.onBackground
                )
            },
            selectedIndex = if (HomeStatusAlignment.fromValue(uiState.homeStatusAlignment) == HomeStatusAlignment.END) 1 else 0,
            onSelectedIndexChange = { index ->
                viewModel.setHomeStatusAlignment(
                    if (index == 1) HomeStatusAlignment.END.value else HomeStatusAlignment.START.value
                )
            },
        )
        MiuixValueSlider(
            title = stringResource(R.string.settings_home_pill_scale),
            summary = stringResource(R.string.settings_home_pill_scale_summary),
            icon = Icons.Rounded.Tune,
            value = uiState.homePillScale,
            valueRange = HomeStatusStyle.PILL_SCALE_MIN..HomeStatusStyle.PILL_SCALE_MAX,
            label = { "${(it * 100).roundToInt()}%" },
            onValueChangeFinished = viewModel::setHomePillScale,
        )
        if (imageStyle == HomeCardImageStyle.BANNER && cardImagePath.isNotBlank()) {
            MiuixValueSlider(
                title = stringResource(R.string.settings_home_status_scale),
                summary = stringResource(R.string.settings_home_status_scale_summary),
                icon = Icons.Rounded.AspectRatio,
                value = uiState.homeStatusScale,
                valueRange = HomeStatusStyle.SCALE_MIN..HomeStatusStyle.SCALE_MAX,
                label = { "${(it * 100).roundToInt()}%" },
                onValueChangeFinished = viewModel::setHomeStatusScale,
            )
            MiuixValueSlider(
                title = stringResource(R.string.settings_home_status_width),
                summary = stringResource(R.string.settings_home_status_width_summary),
                icon = Icons.Rounded.AspectRatio,
                value = uiState.homeStatusWidthFraction,
                valueRange = HomeStatusStyle.WIDTH_MIN..HomeStatusStyle.WIDTH_MAX,
                label = { "${(it * 100).roundToInt()}%" },
                onValueChangeFinished = viewModel::setHomeStatusWidthFraction,
            )
        }
    }

    MiuixHomeTitleRow(
        value = uiState.customHomeTitle,
        onValueChange = viewModel::setCustomHomeTitle,
    )

    MiuixStickerEntry(
        uiState = uiState,
        viewModel = viewModel,
        onPickSticker = onPickSticker,
        onRemoveSticker = onRemoveSticker,
    )
}

/** Slider row with a live label; the parent keeps the committed value. */
@Composable
private fun MiuixValueSlider(
    title: String,
    summary: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    label: (Float) -> String,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: (Float) -> Unit,
) {
    var draft by remember(value) { mutableFloatStateOf(value) }
    BasicComponent(
        title = title,
        summary = summary,
        startAction = {
            Icon(
                icon,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = title,
                tint = colorScheme.onBackground
            )
        },
        endActions = {
            Text(
                text = label(draft),
                color = colorScheme.onSurfaceVariantActions,
            )
        },
        bottomAction = {
            Slider(
                value = draft,
                onValueChange = {
                    draft = it
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    draft = draft.coerceIn(valueRange)
                    onValueChangeFinished(draft)
                },
                valueRange = valueRange,
                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
            )
        },
    )
}

@Composable
private fun MiuixHomeImagePreview(path: String, contentDescription: String) {
    val file = path.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(24.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        Icon(
            Icons.Rounded.Image,
            modifier = Modifier.padding(end = 6.dp),
            contentDescription = contentDescription,
            tint = colorScheme.onBackground,
        )
    }
}

/** Custom home title row + edit dialog. */
@Composable
private fun MiuixHomeTitleRow(value: String, onValueChange: (String) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    ArrowPreference(
        title = stringResource(R.string.settings_home_title),
        summary = value.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.custom_text_default_value, stringResource(R.string.app_name)),
        startAction = {
            Icon(
                Icons.Rounded.Edit,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_home_title),
                tint = colorScheme.onBackground,
            )
        },
        onClick = { showDialog = true },
    )
    if (showDialog) {
        var draft by remember { mutableStateOf(value) }
        OverlayDialog(
            show = true,
            title = stringResource(R.string.settings_home_title),
            summary = stringResource(R.string.settings_home_title_hint),
            onDismissRequest = { showDialog = false },
            content = {
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = stringResource(R.string.cancel),
                        onClick = { showDialog = false },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = stringResource(android.R.string.ok),
                        onClick = {
                            onValueChange(draft.trim())
                            showDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            },
        )
    }
}

@Composable
private fun MiuixStickerEntry(
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
            BasicComponent(
                title = stringResource(R.string.settings_home_card_sticker),
                summary = stringResource(
                    if (bannerVisible && !hasBackground && !uiState.showHomeCardImageWithoutBackground) {
                        R.string.settings_home_card_sticker_hidden
                    } else {
                        R.string.settings_home_card_sticker_banner_only
                    }
                ),
                startAction = {
                    Icon(
                        Icons.Rounded.Star,
                        modifier = Modifier.padding(end = 6.dp),
                        contentDescription = stringResource(R.string.settings_home_card_sticker),
                        tint = colorScheme.onBackground,
                    )
                },
            )
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
    ArrowPreference(
        title = stringResource(R.string.settings_home_card_sticker),
        summary = summary,
        startAction = {
            Icon(
                Icons.Rounded.Star,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_home_card_sticker),
                tint = colorScheme.onBackground,
            )
        },
        onClick = { expanded = !expanded },
    )
    AnimatedVisibility(visible = expanded) {
        MiuixStickerEditor(
            uiState = uiState,
            viewModel = viewModel,
            onPickSticker = onPickSticker,
            onRemoveSticker = onRemoveSticker,
        )
    }
}

/** Sticker editor: draggable stage, thumbnail strip, add/remove and rotation/size sliders. */
@Composable
private fun MiuixStickerEditor(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onPickSticker: () -> Unit,
    onRemoveSticker: (Int) -> Unit,
) {
    val stickers = uiState.homeCardStickers
    val layout = HomeLayout.fromValue(uiState.homeLayout)
    val immersive = layout == HomeLayout.IMMERSIVE
    var draft by remember { mutableStateOf(stickers) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(stickers) { draft = stickers }
    LaunchedEffect(draft.size) {
        if (selected > draft.lastIndex) selected = draft.lastIndex.coerceAtLeast(0)
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
            containerColor = if (immersive) colorScheme.surfaceContainer else colorScheme.secondaryContainer,
            contentColor = if (immersive) colorScheme.onSurface else colorScheme.onSecondaryContainer,
            accentColor = colorScheme.primary,
            accentContentColor = colorScheme.onPrimary,
            layout = layout,
            imageStyle = HomeCardImageStyle.fromValue(uiState.homeCardImageStyle),
            statusScale = uiState.homeStatusScale,
            statusWidthFraction = uiState.homeStatusWidthFraction,
            pillScale = uiState.homePillScale,
            statusAlignment = HomeStatusAlignment.fromValue(uiState.homeStatusAlignment),
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            draft.forEachIndexed { index, sticker ->
                HomeCardStickerThumbnail(
                    path = sticker.path,
                    selected = index == selected,
                    accentColor = colorScheme.primary,
                    onClick = { selected = index },
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (draft.size < HomeCardSticker.MAX_COUNT) {
                TextButton(
                    text = stringResource(R.string.settings_home_card_sticker_add),
                    onClick = onPickSticker,
                    minWidth = 0.dp,
                    minHeight = 0.dp,
                    cornerRadius = 100.dp,
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_limit),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
            if (draft.isNotEmpty()) {
                TextButton(
                    text = stringResource(R.string.settings_home_card_sticker_remove),
                    onClick = { onRemoveSticker(selected.coerceIn(0, draft.lastIndex)) },
                    minWidth = 0.dp,
                    minHeight = 0.dp,
                    cornerRadius = 100.dp,
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                )
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
            MiuixValueSlider(
                title = stringResource(R.string.settings_home_card_sticker_rotation),
                summary = "",
                icon = Icons.Rounded.Tune,
                value = transform.rotation,
                valueRange = HomeCardStickerTransform.ROTATION_MIN..HomeCardStickerTransform.ROTATION_MAX,
                label = { "${it.roundToInt()}°" },
                onValueChange = { value ->
                    draft = draft.mapIndexed { i, sticker ->
                        if (i == index) sticker.copy(transform = sticker.transform.copy(rotation = value)) else sticker
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
            )
            MiuixValueSlider(
                title = stringResource(R.string.settings_home_card_sticker_size),
                summary = "",
                icon = Icons.Rounded.Tune,
                value = transform.size.coerceIn(HomeCardStickerTransform.SIZE_MIN, sizeMax),
                valueRange = HomeCardStickerTransform.SIZE_MIN..sizeMax,
                label = { "${(it * 100).roundToInt()}%" },
                onValueChange = { value ->
                    draft = draft.mapIndexed { i, sticker ->
                        if (i == index) sticker.copy(transform = sticker.transform.copy(size = value)) else sticker
                    }
                },
                onValueChangeFinished = {
                    draft.getOrNull(index)?.let { latest ->
                        viewModel.updateHomeCardSticker(index, latest.transform)
                    }
                },
            )
        }
    }
}

/** BOTTOM BAR: the four tab icon slot rows plus a clear-all action. */
@Composable
private fun MiuixBottomBarIconRows(
    iconPaths: Map<String, String>,
    onPick: (BottomBarIconSlot) -> Unit,
    onClear: (BottomBarIconSlot) -> Unit,
    onClearAll: () -> Unit,
) {
    BasicComponent(
        title = stringResource(R.string.settings_bottom_bar_icons_title),
        summary = stringResource(R.string.settings_bottom_bar_icons_summary),
        startAction = {
            Icon(
                Icons.Rounded.Apps,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = stringResource(R.string.settings_bottom_bar_icons_title),
                tint = colorScheme.onBackground,
            )
        },
    )
    BottomBarIconSlot.entries.forEach { slot ->
        val path = iconPaths[slot.key]?.takeIf { it.isNotBlank() }
        val label = bottomBarSlotLabel(slot)
        ArrowPreference(
            title = label,
            summary = stringResource(
                if (path != null) {
                    R.string.settings_bottom_bar_icon_custom_summary
                } else {
                    R.string.settings_bottom_bar_icon_default_summary
                }
            ),
            startAction = {
                MiuixBottomBarSlotPreview(
                    path = path,
                    icon = bottomBarSlotIcon(slot),
                    contentDescription = label,
                )
            },
            endActions = {
                if (path != null) {
                    TextButton(
                        text = stringResource(R.string.settings_clear),
                        onClick = { onClear(slot) },
                    )
                }
            },
            onClick = { onPick(slot) },
        )
    }
    if (iconPaths.isNotEmpty()) {
        BasicComponent(
            title = stringResource(R.string.settings_bottom_bar_icons_clear_all),
            summary = stringResource(R.string.settings_bottom_bar_icons_clear_all_summary),
            startAction = {
                Icon(
                    Icons.Rounded.Restore,
                    modifier = Modifier.padding(end = 6.dp),
                    contentDescription = stringResource(R.string.settings_bottom_bar_icons_clear_all),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = onClearAll,
        )
    }
}

@Composable
private fun MiuixBottomBarSlotPreview(
    path: String?,
    icon: ImageVector,
    contentDescription: String,
) {
    val file = path?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(24.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        Icon(
            icon,
            modifier = Modifier.padding(end = 6.dp),
            contentDescription = contentDescription,
            tint = colorScheme.onBackground,
        )
    }
}

@Composable
private fun bottomBarSlotLabel(slot: BottomBarIconSlot): String = stringResource(
    when (slot) {
        BottomBarIconSlot.Home -> R.string.settings_bottom_bar_icon_home
        BottomBarIconSlot.SuperUser -> R.string.settings_bottom_bar_icon_superuser
        BottomBarIconSlot.Module -> R.string.settings_bottom_bar_icon_module
        BottomBarIconSlot.Setting -> R.string.settings_bottom_bar_icon_setting
    }
)

private fun bottomBarSlotIcon(slot: BottomBarIconSlot): ImageVector = when (slot) {
    BottomBarIconSlot.Home -> Icons.Rounded.Wallpaper
    BottomBarIconSlot.SuperUser -> Icons.Rounded.AccountCircle
    BottomBarIconSlot.Module -> Icons.Rounded.Apps
    BottomBarIconSlot.Setting -> Icons.Rounded.Tune
}
