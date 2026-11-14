package me.weishu.kernelsu.ui.navigation3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.data.script.ScriptRepository
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.screen.scriptrun.ScriptRunArg
import me.weishu.kernelsu.ui.util.DownloadService
import me.weishu.kernelsu.ui.util.ThemePackageCrypto
import me.weishu.kernelsu.ui.util.ThemePackageStorage
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.webui.WebUIActivity

private const val SCHEME_KSU = "ksu"
private const val HOST_ACTION = "action"
private const val HOST_WEBUI = "webui"
private const val PARAM_ID = "id"
private const val PARAM_TOKEN = "token"
private const val THEME_SCHEME = "sukisu-theme"
private val THEME_ID_PATTERN = Regex("^[a-z0-9](?:[a-z0-9-]{0,62}[a-z0-9])?$")

/**
 * Resolved intent action to execute after validation.
 */
private sealed interface PendingAction {
    /** Install module(s) from URI — triggered by DownloadService notification or ZIP file open. */
    data class InstallModule(
        val uri: Uri,
        val displayName: String,
        val requiresConfirmation: Boolean,
    ) : PendingAction {
        companion object {
            val InstallModuleSaver = listSaver<InstallModule?, Any>(
                save = { module ->
                    if (module == null) {
                        emptyList()
                    } else {
                        listOf(
                            module.uri,
                            module.displayName,
                            module.requiresConfirmation
                        )
                    }
                },
                restore = { list ->
                    if (list.isEmpty()) {
                        null
                    } else {
                        InstallModule(
                            uri = list[0] as Uri,
                            displayName = list[1] as String,
                            requiresConfirmation = list[2] as Boolean
                        )
                    }
                }
            )
        }
    }

    /** Execute a module's action script — triggered by shortcut or deep link. */
    data class ExecuteAction(val moduleId: String) : PendingAction

    /** Open a module's WebUI — triggered by shortcut. */
    data class OpenWebUI(val moduleId: String) : PendingAction
}

private sealed interface KsuDeepLink {
    data class Action(val moduleId: String) : KsuDeepLink
    data class WebUi(val moduleId: String) : KsuDeepLink
}

private fun buildInternalWebUiUri(moduleId: String): Uri {
    return Uri.Builder()
        .scheme(SCHEME_KSU)
        .authority(HOST_WEBUI)
        .appendQueryParameter(PARAM_ID, moduleId)
        .build()
}

fun getDisplayName(uri: Uri): String {
    return uri.getFileName(ksuApp) ?: uri.lastPathSegment ?: "Unknown"
}

/**
 * Resolve an intent snapshot into a [PendingAction].
 * Returns null if the intent carries no recognized action.
 */
private fun resolveIntent(intent: Intent): PendingAction? {
    // DownloadService notification: install module
    if (intent.action == DownloadService.ACTION_INSTALL_MODULE) {
        val token = intent.getStringExtra(DownloadService.EXTRA_TOKEN)?.takeIf { it.isNotBlank() } ?: return null
        if (token != SettingsRepositoryImpl().intentToken) return null
        val uriString = intent.getStringExtra(DownloadService.EXTRA_MODULE_URI)
            ?: return null
        val uri = uriString.toUri()
        return PendingAction.InstallModule(
            uri = uri,
            displayName = getDisplayName(uri),
            requiresConfirmation = false,
        )
    }

    // File manager: open ZIP
    getZipFileViewUri(intent)?.let { viewUri ->
        return PendingAction.InstallModule(
            uri = viewUri,
            displayName = getDisplayName(viewUri),
            requiresConfirmation = true,
        )
    }

    // Check deep links
    return when (val deepLink = parseValidatedDeepLink(intent.data)) {
        is KsuDeepLink.Action -> PendingAction.ExecuteAction(deepLink.moduleId)
        is KsuDeepLink.WebUi -> PendingAction.OpenWebUI(deepLink.moduleId)
        null -> null
    }
}

private fun parseThemeStoreLink(intent: Intent): Route.ThemeStore? {
    if (intent.action != Intent.ACTION_VIEW) return null
    val uri = intent.data ?: return null
    if (uri.scheme != THEME_SCHEME || uri.host != "open") return null
    val id = uri.getQueryParameter("id")?.takeIf { THEME_ID_PATTERN.matches(it) } ?: return null
    // This URL only selects an already-configured source; it never installs a token or adds a server.
    val source = uri.getQueryParameter("source")?.takeIf { it.length <= 256 }
        ?.takeIf { Uri.parse(it).scheme in setOf("http", "https") }
        ?.trimEnd('/')
    return Route.ThemeStore(themeId = id, sourceBaseUrl = source)
}

private suspend fun extractThemePackageUri(context: Context, intent: Intent): Uri? =
    withContext(Dispatchers.IO) {
        val uris = linkedSetOf<Uri>()

        fun add(uri: Uri?) {
            if (uri != null) uris.add(uri)
        }

        add(intent.data)
        when (intent.action) {
            Intent.ACTION_VIEW, Intent.ACTION_SEND -> add(intent.getThemeStreamUri())
            Intent.ACTION_SEND_MULTIPLE -> intent.getThemeStreamUris()?.forEach(::add)
        }

        val clipData = intent.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                add(clipData.getItemAt(i)?.uri)
            }
        }

        if (uris.isEmpty()) return@withContext null

        uris.firstOrNull { isThemePackageUri(context, intent, it) }
            ?: uris.firstOrNull { hasThemeHeader(context, it) }
    }

private fun Intent.getThemeStreamUri(): Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
} else {
    @Suppress("DEPRECATION")
    getParcelableExtra(Intent.EXTRA_STREAM)
}

private fun Intent.getThemeStreamUris(): List<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
} else {
    @Suppress("DEPRECATION")
    getParcelableArrayListExtra(Intent.EXTRA_STREAM)
}

private fun isThemePackageUri(context: Context, intent: Intent, uri: Uri): Boolean {
    val fileName = uri.getFileName(context) ?: uri.lastPathSegment.orEmpty()
    val cleanName = fileName.substringBefore('?')
    if (cleanName.endsWith(".sukisu", ignoreCase = true)) return true

    val declaredType = intent.type
        ?.substringBefore(';')
        ?.trim()
        ?.lowercase()
    if (declaredType == ThemePackageStorage.THEME_MIME_TYPE) return true

    val resolvedType = runCatching { context.contentResolver.getType(uri) }
        .getOrNull()
        ?.substringBefore(';')
        ?.trim()
        ?.lowercase()
    return resolvedType == ThemePackageStorage.THEME_MIME_TYPE
}

/**
 * Shared files often arrive as octet-stream with a content URI that has no usable display
 * name, so filename and MIME checks are not enough. The encrypted package header is stable
 * and cheap to read, which lets us tell themes apart from module ZIPs before routing.
 */
private fun hasThemeHeader(context: Context, uri: Uri): Boolean = runCatching {
    context.contentResolver.openInputStream(uri)?.use { input ->
        val header = ByteArray(ThemePackageCrypto.HEADER_SIZE)
        var read = 0
        while (read < header.size) {
            val count = input.read(header, read, header.size - read)
            if (count < 0) break
            read += count
        }
        ThemePackageCrypto.hasValidHeader(header.copyOf(read))
    } ?: false
}.getOrDefault(false)

private fun parseValidatedDeepLink(uri: Uri?): KsuDeepLink? {
    if (uri?.scheme != SCHEME_KSU) return null

    val moduleId = uri.getQueryParameter(PARAM_ID)?.takeIf { it.isNotBlank() } ?: return null
    val token = uri.getQueryParameter(PARAM_TOKEN)?.takeIf { it.isNotBlank() } ?: return null
    if (token != SettingsRepositoryImpl().intentToken) return null

    return when (uri.host) {
        HOST_ACTION -> KsuDeepLink.Action(moduleId)
        HOST_WEBUI -> KsuDeepLink.WebUi(moduleId)
        else -> null
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
fun IntentDispatcher(intentChannel: ReceiveChannel<Intent>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navigator = LocalNavigator.current
    val isSafeMode = Natives.isSafeMode
    val isManager = Natives.isManager
    var pendingZipInstall by rememberSaveable(stateSaver = PendingAction.InstallModule.InstallModuleSaver) { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    // A one-shot script ZIP opens the run page, which also offers installing it as a module.
    val openModuleZip: (Uri) -> Unit = { uri ->
        scope.launch {
            val script = ScriptRepository.peekOneshot(uri)
            if (script != null) {
                navigator.push(Route.ScriptRun(ScriptRunArg(uri, offerInstall = true, displayName = script.name)))
            } else {
                navigator.push(Route.Flash(FlashIt.FlashModules(listOf(uri))))
            }
        }
    }

    val installDialog = rememberConfirmDialog(
        onConfirm = {
            pendingZipInstall?.let { action -> openModuleZip(action.uri) }
            pendingZipInstall = null
        },
        onDismiss = { pendingZipInstall = null }
    )

    CollectIntentChannel(intentChannel) { intent ->
        if (!isManager) return@CollectIntentChannel

        parseThemeStoreLink(intent)?.let { destination ->
            navigator.push(destination)
            return@CollectIntentChannel
        }

        val themeUri = extractThemePackageUri(context, intent)
        if (themeUri != null) {
            // Forward the URI through the route. The appearance destination owns a per-entry
            // SettingsViewModel, so setting dialog state on the activity-scoped dispatcher
            // view model would never be observed by the screen that renders the dialog.
            navigator.push(Route.ColorPalette(importUri = themeUri.toString()))
            return@CollectIntentChannel
        }

        val action = resolveIntent(intent) ?: return@CollectIntentChannel

        when (action) {
            is PendingAction.InstallModule -> {
                if (isSafeMode) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.safe_mode_module_disabled),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@CollectIntentChannel
                }
                if (action.requiresConfirmation) {
                    pendingZipInstall = action
                    installDialog.showConfirm(
                        title = resources.getString(R.string.module),
                        content = resources.getString(
                            R.string.module_install_prompt_with_name,
                            "\n${action.displayName}"
                        )
                    )
                } else {
                    openModuleZip(action.uri)
                }
            }

            is PendingAction.ExecuteAction -> {
                navigator.push(Route.ExecuteModuleAction(action.moduleId, fromShortcut = true))
            }

            is PendingAction.OpenWebUI -> {
                val webIntent = Intent(context, WebUIActivity::class.java)
                    .setData(buildInternalWebUiUri(action.moduleId))
                context.startActivity(webIntent)
            }
        }
    }
}

/**
 * Receive intents inside a [LaunchedEffect] tied to the channel identity.
 * Each emitted intent is processed exactly once; no activity.intent mutation needed.
 */
@Composable
private fun CollectIntentChannel(intentChannel: ReceiveChannel<Intent>, onIntent: suspend (Intent) -> Unit) {
    LaunchedEffect(intentChannel) {
        for (intent in intentChannel) {
            onIntent(intent)
        }
    }
}
