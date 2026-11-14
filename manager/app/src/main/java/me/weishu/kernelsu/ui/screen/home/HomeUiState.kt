package me.weishu.kernelsu.ui.screen.home

import androidx.compose.runtime.Immutable
import me.weishu.kernelsu.KernelVersion
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo

@Immutable
data class HomeUiState(
    val kernelVersion: KernelVersion,
    val ksuVersion: Int?,
    val managerUAPIVersion: Int,
    val kernelUAPIVersion: Int?,
    val lkmMode: Boolean?,
    val isLkmBundled: Boolean,
    val isManager: Boolean,
    val isManagerPrBuild: Boolean,
    val isKernelPrBuild: Boolean,
    val requiresNewKernel: Boolean,
    val requiresNewManager: Boolean,
    val isRootAvailable: Boolean,
    val isSafeMode: Boolean,
    val isLateLoadMode: Boolean,
    val checkUpdateEnabled: Boolean,
    val latestVersionInfo: LatestVersionInfo,
    val currentManagerVersionCode: Long,
    val systemInfo: SystemInfo,
    val showFullStatus: Boolean = true,
    val hideHomeWarnings: Boolean = false,
    val customHomeTitle: String = "",
    val customTexts: Map<String, String> = emptyMap(),
    val homeCardImagePath: String = "",
    val hideHomeCardStatusText: Boolean = false,
    val hideHomeCardDecoration: Boolean = false,
    val homeCardStickers: List<HomeCardSticker> = emptyList(),
    val homeLayout: HomeLayout = HomeLayout.CLASSIC,
    val homeStatusAlignment: HomeStatusAlignment = HomeStatusAlignment.START,
    val homeStatusScale: Float = 1f,
    val homePillScale: Float = 1f,
    val homeStatusWidthFraction: Float = 1f,
    val homeCardImageStyle: HomeCardImageStyle = HomeCardImageStyle.BANNER,
    val showHomeCardImageWithoutBackground: Boolean = false,
) {
    val isSELinuxPermissive: Boolean
        get() = systemInfo.selinuxStatus == "Permissive"

    val showGkiWarning: Boolean
        get() = ksuVersion != null && lkmMode == false

    // The embedded LKM ships with the manager, so "update available" only makes sense when the
    // running (bundled) LKM is older than this manager. A newer running LKM must not be reported
    // as updatable: patching again would silently downgrade it.
    val showLkmUpdate: Boolean
        get() = isManager &&
                lkmMode == true &&
                isLkmBundled &&
                (ksuVersion?.toLong() ?: 0L) < currentManagerVersionCode &&
                !requiresNewKernel &&
                !requiresNewManager

    // Jailbreak mode runs on locked bootloaders, so flashing a boot image would brick the device.
    val canInstallKernelUpdate: Boolean
        get() = lkmMode == true && !isLateLoadMode

    val showCustomLkmBadge: Boolean
        get() = lkmMode == true && !isLkmBundled

    /**
     * Working-mode tag on the home status card.
     *
     * LKM is a 5.10+ concept; a pre-5.10 kernel always runs the built-in
     * driver, so show "Built-in" there instead of leaving the tag blank.
     * On 5.10+ keep the original mapping: true = LKM, false = Built-in,
     * unknown (null) = no tag. With no active kernel (no root) there is
     * no mode to show either.
     */
    val workingModeLabel: String
        get() = when {
            ksuVersion == null -> ""
            !kernelVersion.isGKI() -> "Built-in"
            lkmMode == true -> "LKM"
            lkmMode == false -> "Built-in"
            else -> ""
        }

    val showRootWarning: Boolean
        get() = ksuVersion != null && !isRootAvailable

    val showManagerPrBuildWarning: Boolean
        get() = isManager && isManagerPrBuild

    val showKernelPrBuildWarning: Boolean
        get() = isManager && !isManagerPrBuild && isKernelPrBuild

    val hasUpdate: Boolean
        get() = latestVersionInfo.versionCode > currentManagerVersionCode
}

@Immutable
data class HomeActions(
    val onInstallClick: () -> Unit,
    val onOpenUrl: (String) -> Unit,
    val onJailbreakClick: () -> Unit = {},
)
