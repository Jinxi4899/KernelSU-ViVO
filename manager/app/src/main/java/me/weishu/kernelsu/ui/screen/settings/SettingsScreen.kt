package me.weishu.kernelsu.ui.screen.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Build
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.navigation3.Navigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

@Composable
fun SettingPager(
    navigator: Navigator,
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true,
) {
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val latestIsCurrentPage by rememberUpdatedState(isCurrentPage)
    val initialResumeHandled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            viewModel.refresh()
        }
    }

    LifecycleResumeEffect(Unit) {
        if (initialResumeHandled.value && latestIsCurrentPage) {
            viewModel.refresh()
        }
        initialResumeHandled.value = true
        onPauseOrDispose { }
    }

    val actions = SettingsScreenActions(
        onSetCheckUpdate = viewModel::setCheckUpdate,
        onSetCheckModuleUpdate = viewModel::setCheckModuleUpdate,
        onOpenTheme = { navigator.push(Route.ColorPalette()) },
        onSetLanguage = { tag ->
            viewModel.setAppLanguage(tag)
            // Android 13+ recreates through LocaleManager; older releases need a manual restart.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) activity?.recreate()
        },
        onSetUiModeIndex = { index ->
            viewModel.setUiMode(if (index == 0) UiMode.Miuix.value else UiMode.Material.value)
        },
        onOpenProfileTemplate = { navigator.push(Route.AppProfileTemplate) },
        onSetSuCompatMode = viewModel::setSuCompatMode,
        onSetKernelUmountEnabled = viewModel::setKernelUmountEnabled,
        onSetSelinuxHideEnabled = viewModel::setSelinuxHideEnabled,
        onSetSulogEnabled = viewModel::setSulogEnabled,
        onSetAdbRootEnabled = viewModel::setAdbRootEnabled,
        onSetShellSuAdbOnlyEnabled = viewModel::setShellSuAdbOnlyEnabled,
        onSetDefaultUmountModules = viewModel::setDefaultUmountModules,
        onSetEnableWebDebugging = viewModel::setEnableWebDebugging,
        onSetAutoJailbreak = viewModel::setAutoJailbreak,
        onSetUseSoftReboot = viewModel::setUseSoftReboot,
        onOpenAbout = { navigator.push(Route.About) },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingPagerMiuix(uiState, actions, bottomInnerPadding)
        UiMode.Material -> SettingPagerMaterial(uiState, actions, bottomInnerPadding)
    }
}
