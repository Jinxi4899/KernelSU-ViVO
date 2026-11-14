package me.weishu.kernelsu.ui.viewmodel

import android.os.Build
import android.system.Os
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.getKernelVersion
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.screen.home.HomeUiState
import me.weishu.kernelsu.ui.screen.home.SystemInfo
import me.weishu.kernelsu.ui.screen.home.getManagerVersion
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.checkNewVersion
import me.weishu.kernelsu.ui.util.getSELinuxStatusRaw
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo
import me.weishu.kernelsu.ui.util.resolveDeviceName
import me.weishu.kernelsu.ui.util.rootAvailable
import java.io.File

class HomeViewModel(
    private val settingsRepo: SettingsRepository = SettingsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val baseState = withContext(Dispatchers.IO) { buildState() }
            _uiState.update { baseState }
            if (baseState.checkUpdateEnabled) {
                val latestVersionInfo = withContext(Dispatchers.IO) { checkNewVersion() }
                _uiState.update { it.copy(latestVersionInfo = latestVersionInfo) }
            }
        }
    }

    private fun existingHomeCardImage(): String =
        settingsRepo.homeCardImagePath.takeIf { it.isNotEmpty() && File(it).isFile } ?: ""

    private fun existingHomeCardStickers(): List<HomeCardSticker> =
        settingsRepo.homeCardStickers.filter { File(it.path).isFile }

    private fun buildState(): HomeUiState {
        val kernelVersion = getKernelVersion()
        val isManager = Natives.isManager
        val ksuVersion = if (isManager) Natives.version else null
        val kernelUAPIVersion = if (isManager) Natives.kernelUAPIVersion else null
        val managerUAPIVersion = Natives.managerUAPIVersion
        val lkmMode = ksuVersion?.let { if (kernelVersion.isGKI()) Natives.isLkmMode else null }
        val isRootAvailable = rootAvailable()
        val managerVersion = getManagerVersion(ksuApp)

        return HomeUiState(
            kernelVersion = kernelVersion,
            ksuVersion = ksuVersion,
            lkmMode = lkmMode,
            isLkmBundled = lkmMode == true && Natives.isLkmBundled,
            isManager = isManager,
            isManagerPrBuild = BuildConfig.IS_PR_BUILD,
            isKernelPrBuild = Natives.isPrBuild,
            requiresNewKernel = isManager && Natives.managerUAPIVersion > Natives.kernelUAPIVersion,
            requiresNewManager = isManager && Natives.managerUAPIVersion < Natives.kernelUAPIVersion,
            kernelUAPIVersion = kernelUAPIVersion,
            managerUAPIVersion = managerUAPIVersion,
            isRootAvailable = isRootAvailable,
            isSafeMode = Natives.isSafeMode,
            isLateLoadMode = Natives.isLateLoadMode,
            checkUpdateEnabled = settingsRepo.checkUpdate,
            showFullStatus = settingsRepo.showFullStatus,
            hideHomeWarnings = settingsRepo.hideHomeWarnings,
            customHomeTitle = settingsRepo.customHomeTitle,
            customTexts = settingsRepo.customTexts,
            homeCardImagePath = existingHomeCardImage(),
            hideHomeCardStatusText = settingsRepo.hideHomeCardStatusText,
            hideHomeCardDecoration = settingsRepo.hideHomeCardDecoration,
            homeCardStickers = existingHomeCardStickers(),
            homeLayout = HomeLayout.fromValue(settingsRepo.homeLayout),
            homeStatusAlignment = HomeStatusAlignment.fromValue(settingsRepo.homeStatusAlignment),
            homeStatusScale = HomeStatusStyle.coerceScale(settingsRepo.homeStatusScale),
            homePillScale = HomeStatusStyle.coercePillScale(settingsRepo.homePillScale),
            homeStatusWidthFraction = HomeStatusStyle.coerceWidth(settingsRepo.homeStatusWidthFraction),
            homeCardImageStyle = HomeCardImageStyle.fromValue(settingsRepo.homeCardImageStyle),
            showHomeCardImageWithoutBackground = settingsRepo.showHomeCardImageWithoutBackground,
            latestVersionInfo = LatestVersionInfo(),
            currentManagerVersionCode = managerVersion.versionCode,
            systemInfo = SystemInfo(
                kernelVersion = Os.uname().release,
                managerVersion = BuildConfig.VERSION_DISPLAY,
                deviceModel = resolveDeviceName(),
                fingerprint = Build.FINGERPRINT,
                selinuxStatus = getSELinuxStatusRaw(),
                seccompStatus = runCatching {
                    Os.prctl(21 /* PR_GET_SECCOMP */, 0, 0, 0, 0)
                }.getOrDefault(-1),
            ),
        )
    }
}
