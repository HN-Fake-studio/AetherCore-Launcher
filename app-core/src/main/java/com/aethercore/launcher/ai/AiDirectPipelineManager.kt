package com.aethercore.launcher.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.io.File

/**
 * AiDirectPipelineManager_(ai): Gathers crash logs, system specs, and exit codes,
 * formats them into a ready-to-use prompt, and launches a browser directly to Gemini.
 */
object AiDirectPipelineManager_(ai) {

    /**
     * Gathers system info and log contents, formats into a prompt, and opens Google Gemini in the browser.
     */
    fun sendLogToGemini(context: Context, logFile: File?, exitCode: Int, customErrorTitle: String) {
        val logContent = if (logFile != null && logFile.exists()) {
            // Read last 100 lines or max 50KB to avoid overly large URL encoding
            try {
                logFile.readText().takeLast(10000)
            } catch (e: Exception) {
                "Unable to read log file: ${e.message}"
            }
        } else {
            "No log file available."
        }

        val deviceModel = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT
        val manufacturer = Build.MANUFACTURER

        val promptBuilder = StringBuilder().apply {
            append("You are an expert Android Minecraft Java Launcher (Zalith/AetherCore) & JVM debugging engineer.\n\n")
            append("### Crash / Error Report Analysis\n")
            append("- **Error Title**: $customErrorTitle\n")
            append("- **Exit Code**: $exitCode\n")
            append("- **Device**: $manufacturer $deviceModel (Android $androidVersion, SDK $sdkInt)\n\n")
            append("### Log Snippet:\n")
            append("```\n$logContent\n```\n\n")
            append("Please diagnose the root cause of this error and provide clear, actionable steps in Thai language to resolve it.")
        }

        val promptText = promptBuilder.toString()
        
        // Encode for URL query / web prompt
        val encodedPrompt = Uri.encode(promptText)
        val geminiUrl = "https://gemini.google.com/app?q=$encodedPrompt"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geminiUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard web search if direct app link fails
            val fallbackUrl = "https://www.google.com/search?q=" + Uri.encode("Minecraft Java Android crash $customErrorTitle")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }
}
