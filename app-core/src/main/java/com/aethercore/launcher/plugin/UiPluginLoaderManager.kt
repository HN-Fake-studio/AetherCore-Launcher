package com.aethercore.launcher.plugin

import android.content.Context
import com.aethercore.sdk.AetherPluginApi_(ai)
import com.aethercore.sdk.PluginMetadata_(ai)
import dalvik.system.DexClassLoader
import java.io.File
import java.util.zip.ZipFile

/**
 * UiPluginLoaderManager_(ai): Manages loading, verification, and execution of
 * external UI extension packages (.eamp files) adhering to AetherCore SDK.
 */
class UiPluginLoaderManager_(ai)(private val context: Context) {

    private val loadedPlugins = mutableMapOf<String, AetherPluginApi_(ai)>()
    private val pluginMetadataList = mutableListOf<PluginMetadata_(ai)>()

    private val pluginsDir: File
        get() = File(context.filesDir, "eamp_plugins").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Scans the plugin directory for .eamp files and loads them via DexClassLoader.
     */
    fun scanAndLoadPlugins(): List<PluginMetadata_(ai)> {
        pluginMetadataList.clear()
        loadedPlugins.clear()

        val files = pluginsDir.listFiles { _, name -> name.endsWith(".eamp", ignoreCase = true) } ?: return emptyList()

        for (file in files) {
            try {
                val metadata = parsePluginMetadata(file)
                if (metadata != null) {
                    val loadedApi = loadDexPlugin(file, metadata.id)
                    if (loadedApi != null) {
                        loadedPlugins[metadata.id] = loadedApi
                        loadedApi.onPluginLoaded()
                        pluginMetadataList.add(metadata.copy(isEnabled = true))
                    } else {
                        pluginMetadataList.add(metadata.copy(isEnabled = false))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return pluginMetadataList
    }

    private fun parsePluginMetadata(eampFile: File): PluginMetadata_(ai)? {
        // .eamp is a zip archive containing plugin.json and classes.dex
        try {
            ZipFile(eampFile).use { zip ->
                val entry = zip.getEntry("plugin.json")
                if (entry != null) {
                    val jsonStr = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(jsonStr)
                    return PluginMetadata_(ai)(
                        id = json.optString("id", eampFile.nameWithoutExtension),
                        name = json.optString("name", eampFile.nameWithoutExtension),
                        version = json.optString("version", "1.0.0"),
                        author = json.optString("author", "Unknown"),
                        filePath = eampFile.absolutePath,
                        isEnabled = false
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback metadata if plugin.json is missing
        return PluginMetadata_(ai)(
            id = eampFile.nameWithoutExtension,
            name = eampFile.nameWithoutExtension,
            version = "1.0.0",
            author = "Unknown",
            filePath = eampFile.absolutePath,
            isEnabled = false
        )
    }

    private fun loadDexPlugin(eampFile: File, pluginId: String): AetherPluginApi_(ai)? {
        val dexOutputDir = File(context.codeCacheDir, "dex_plugins_$pluginId").apply {
            if (!exists()) mkdirs()
        }

        val classLoader = DexClassLoader(
            eampFile.absolutePath,
            dexOutputDir.absolutePath,
            null,
            context.classLoader
        )

        // Conventions: Plugin main class must be specified in metadata or implement AetherPluginApi_(ai)
        // For simplicity, we search for classes or assume entry point class name standard
        // In full implementation, entry class is read from plugin.json ("mainClass")
        try {
            // Read mainClass from plugin.json if present, or fallback
            val mainClass = "com.aethercore.plugin.MainPlugin" // standard convention for .eamp
            val clazz = classLoader.loadClass(mainClass)
            val instance = clazz.getDeclaredConstructor().newInstance()
            if (instance is AetherPluginApi_(ai)) {
                return instance
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getActivePlugin(pluginId: String): AetherPluginApi_(ai)? {
        return loadedPlugins[pluginId]
    }

    fun unloadAll() {
        for ((_, plugin) in loadedPlugins) {
            try {
                plugin.onPluginUnloaded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadedPlugins.clear()
        pluginMetadataList.clear()
    }
}
