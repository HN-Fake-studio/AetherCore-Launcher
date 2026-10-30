package com.aethercore.launcher.runtime

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * ModularRuntimeDownloaderManager_(ai): Manages on-demand downloading of JRE 17/21
 * and native rendering libraries (Vulkan, Zink, MobileGlues) to keep base APK slim (<100MB).
 */
class ModularRuntimeDownloaderManager_(ai)(private val context: Context) {

    private val client = OkHttpClient()

    enum class RuntimeComponent(val fileName: String, val downloadUrl: String, val targetSubDir: String) {
        JRE_17(
            "jre-17-linux-aarch64.tar.xz",
            "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9+9/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.9_9.tar.gz",
            "runtimes/jre17"
        ),
        JRE_21(
            "jre-21-linux-aarch64.tar.xz",
            "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.1_12.tar.gz",
            "runtimes/jre21"
        ),
        RENDERER_VULKAN_ZINK(
            "libvulkan_zink.so",
            "https://raw.githubusercontent.com/ZalithLauncher/ZalithLauncher2/main/runtimes/libvulkan_zink.so",
            "runtimes/renderers"
        )
    }

    fun isComponentInstalled(component: RuntimeComponent): Boolean {
        val dir = File(context.filesDir, component.targetSubDir)
        val file = File(dir, component.fileName)
        return file.exists() && file.length() > 0
    }

    suspend fun downloadComponent(
        component: RuntimeComponent,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(0f, "กำลังเชื่อมต่อเพื่อดาวน์โหลด ${component.name}...")
            val request = Request.Builder().url(component.downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext false

            val body = response.body ?: return@withContext false
            val contentLength = body.contentLength()

            val targetDir = File(context.filesDir, component.targetSubDir).apply {
                if (!exists()) mkdirs()
            }
            val outputFile = File(targetDir, component.fileName)

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (contentLength > 0) {
                            val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                            onProgress(progress, "กำลังดาวน์โหลด... ${(progress * 100).toInt()}%")
                        }
                    }
                    output.flush()
                }
            }
            onProgress(1f, "ดาวน์โหลด ${component.name} สำเร็จสมบูรณ์")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(0f, "เกิดข้อผิดพลาด: ${e.localizedMessage}")
            false
        }
    }
}
