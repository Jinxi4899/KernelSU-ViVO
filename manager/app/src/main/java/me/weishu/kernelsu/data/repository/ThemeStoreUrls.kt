package me.weishu.kernelsu.data.repository

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * URL helpers shared by the store repository, the default-mirror picker and the access
 * repository. Keeping them here avoids a dependency cycle between those three.
 */
object ThemeStoreUrls {

    /** Normalizes a user or catalog supplied mirror into an absolute URL ending with `/`. */
    fun normalizeBaseUrl(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        val url = withScheme.toHttpUrlOrNull() ?: return null
        if (url.query != null || url.fragment != null) return null
        if (url.encodedPath.endsWith(".json")) return null
        return url.toString().trimEnd('/') + "/"
    }

    /**
     * Resolves a catalog-relative asset path. Absolute URLs and `..` segments are rejected so a
     * compromised catalog cannot point the downloader at another host or escape the store root.
     */
    fun assetUrl(baseUrl: String, path: String): String? {
        val base = normalizeBaseUrl(baseUrl) ?: return null
        if (path.isBlank() || path.contains("..") ||
            path.startsWith("http://") || path.startsWith("https://")
        ) {
            return null
        }
        return base + path.removePrefix("/")
    }
}
