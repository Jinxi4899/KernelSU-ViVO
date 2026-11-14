package me.weishu.kernelsu.data.model

import kotlinx.serialization.Serializable

/** One additional theme-store mirror configured by the user. */
@Serializable
data class ThemeStoreSourceConfig(
    val id: String,
    val name: String = "",
    val baseUrl: String = "",
    val token: String = "",
    val adminUrl: String = "",
)
