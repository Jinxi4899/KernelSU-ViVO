package me.weishu.kernelsu.ui.screen.home

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.core.content.pm.PackageInfoCompat
import java.io.File

@Immutable
data class ManagerVersion(
    val versionName: String,
    val versionCode: Long
)

@Immutable
data class SystemInfo(
    val kernelVersion: String,
    val managerVersion: String,
    val deviceModel: String,
    val fingerprint: String,
    val selinuxStatus: String,
    val seccompStatus: Int
)

/**
 * Coil cache key for the home card image. Including the file timestamp makes a replaced image
 * at the same path invalidate the previous bitmap instead of serving a stale card.
 */
fun homeCardImageCacheKey(path: String): String {
    val file = File(path)
    return "home-card:$path:${file.lastModified()}"
}

fun getManagerVersion(context: Context): ManagerVersion {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return ManagerVersion(
        versionName = packageInfo.versionName!!,
        versionCode = versionCode
    )
}
