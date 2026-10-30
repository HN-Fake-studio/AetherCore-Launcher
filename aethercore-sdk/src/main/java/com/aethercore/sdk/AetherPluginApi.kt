package com.aethercore.sdk

import androidx.compose.runtime.Composable

/**
 * AetherPluginApi_(ai): Interface definition for AetherCore Launcher UI Plugins (.eamp).
 * External developers implement this interface to provide custom home screens, widgets, or themes.
 */
interface AetherPluginApi_(ai) {
    val pluginId: String
    val pluginName: String
    val pluginVersion: String
    val author: String

    @Composable
    fun RenderCustomHomeScreen(onLaunchGame: (String) -> Unit)

    @Composable
    fun RenderCustomWidget()

    fun onPluginLoaded()
    fun onPluginUnloaded()
}

/**
 * Metadata descriptor for loaded plugins.
 */
data class PluginMetadata_(ai)(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val filePath: String,
    val isEnabled: Boolean
)
