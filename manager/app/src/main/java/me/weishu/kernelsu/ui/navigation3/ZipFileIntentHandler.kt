package me.weishu.kernelsu.ui.navigation3

import android.content.Intent
import android.net.Uri

/**
 * MIME types accepted for module and kernel ZIP files opened from a file manager.
 */
internal val ZIP_MIME_TYPES = setOf(
    "application/zip",
    "application/x-zip",
    "application/x-zip-compressed",
)

/**
 * Returns the ZIP/APK URI carried by a file-manager VIEW intent, or null when the intent is
 * not a ZIP open request. Theme packages are filtered out by [IntentDispatcher] before this
 * check, and install confirmation stays in the dispatcher so the existing target behaviour
 * for modules, kernel ZIPs and shortcuts is preserved.
 */
internal fun getZipFileViewUri(intent: Intent): Uri? {
    val uri = intent.data ?: return null
    if (uri.scheme != "content") return null
    return uri.takeIf { intent.type in ZIP_MIME_TYPES }
}
