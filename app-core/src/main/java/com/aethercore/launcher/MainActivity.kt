package com.aethercore.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aethercore.launcher.ai.AiDirectPipelineManager_(ai)
import com.aethercore.launcher.plugin.UiPluginLoaderManager_(ai)
import com.aethercore.launcher.runtime.ModularRuntimeDownloaderManager_(ai)
import com.aethercore.launcher.switcher.MultiScenarioModeSwitcherManager_(ai)
import kotlinx.coroutines.launch
import java.io.File

class MainActivity_(ai) : ComponentActivity() {

    private lateinit var modeSwitcher: MultiScenarioModeSwitcherManager_(ai)
    private lateinit var runtimeDownloader: ModularRuntimeDownloaderManager_(ai)
    private lateinit var pluginLoader: UiPluginLoaderManager_(ai)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modeSwitcher = MultiScenarioModeSwitcherManager_(ai)(this)
        runtimeDownloader = ModularRuntimeDownloaderManager_(ai)(this)
        pluginLoader = UiPluginLoaderManager_(ai)(this)

        setContent {
            MaterialTheme(
                colorScheme = if (modeSwitcher.currentMode == MultiScenarioModeSwitcherManager_(ai).AppMode.BATTERY_SAVER) {
                    darkColorScheme() // OLED Black style approximation
                } else {
                    lightColorScheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        context = this,
                        modeSwitcher = modeSwitcher,
                        runtimeDownloader = runtimeDownloader,
                        pluginLoader = pluginLoader
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: android.content.Context,
    modeSwitcher: MultiScenarioModeSwitcherManager_(ai),
    runtimeDownloader: ModularRuntimeDownloaderManager_(ai),
    pluginLoader: UiPluginLoaderManager_(ai)
) {
    var currentMode by remember { mutableStateOf(modeSwitcher.currentMode) }
    var downloadStatus by remember { mutableStateOf("พร้อมดาวน์โหลด Runtime") }
    var downloadProgress by remember { mutableStateOf(0f) }
    var plugins by remember { mutableStateOf(pluginLoader.scanAndLoadPlugins()) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AetherCore Launcher (ai)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Multi-Scenario Mode Switcher
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎮 โหมดการใช้งานปัจจุบัน: ${currentMode.modeName}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentMode.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        MultiScenarioModeSwitcherManager_(ai).AppMode.values().forEach { mode ->
                            Button(
                                onClick = {
                                    modeSwitcher.currentMode = mode
                                    currentMode = mode
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(mode.modeName)
                            }
                        }
                    }
                }
            }

            // Section 2: Modular Runtime Downloader
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📦 ระบบดาวน์โหลดรันไทม์ On-Demand", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(downloadStatus, style = MaterialTheme.typography.bodySmall)
                        if (downloadProgress > 0f && downloadProgress < 1f) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    runtimeDownloader.downloadComponent(
                                        ModularRuntimeDownloaderManager_(ai).RuntimeComponent.JRE_17
                                    ) { progress, message ->
                                        downloadProgress = progress
                                        downloadStatus = message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ดาวน์โหลด JRE 17 Runtime")
                        }
                    }
                }
            }

            // Section 3: AI Direct Pipeline System
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 AI Direct Pipeline System", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ส่งรายงานข้อผิดพลาดและ Log ล่าสุดให้ Gemini วิเคราะห์ทันทีใน 1 คลิก", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                // Example mock crash log file for test/demo
                                val mockLog = File(context.filesDir, "latest.log").apply {
                                    if (!exists()) {
                                        writeText("[AetherCore] Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space\n    at net.minecraft.client.Main.main(Main.java:123)")
                                    }
                                }
                                AiDirectPipelineManager_(ai).sendLogToGemini(
                                    context = context,
                                    logFile = mockLog,
                                    exitCode = 137,
                                    customErrorTitle = "OutOfMemoryError during Minecraft launch"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("🤖 ส่งวิเคราะห์ด่วนให้ Gemini AI")
                        }
                    }
                }
            }

            // Section 4: UI Plugin Loader (.eamp)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🧩 ระบบส่วนเสริม UI (.eamp Plugins)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ปลั๊กอินที่ติดตั้งแล้ว: ${plugins.size} ตัว", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                plugins = pluginLoader.scanAndLoadPlugins()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("สแกนและโหลดปลั๊กอินใหม่")
                        }
                    }
                }
            }
        }
    }
}
