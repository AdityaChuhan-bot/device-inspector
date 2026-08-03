package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.io.File
import java.util.Locale

/**
 * Core data structure holding system metrics gathered via Build and ActivityManager.
 */
data class CoreSystemMetrics(
    val cpuModel: String,
    val totalRamBytes: Long,
    val totalRamFormatted: String,
    val availableRamBytes: Long,
    val availableRamFormatted: String,
    val osVersion: String,
    val sdkInt: Int,
    val deviceModel: String,
    val manufacturer: String,
    val abis: List<String>
)

/**
 * SystemInfoProvider gathers system metrics using Android's Build and ActivityManager APIs.
 */
class SystemInfoProvider(private val context: Context) {

    private val activityManager: ActivityManager? by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    }

    /**
     * Retrieve ActivityManager.MemoryInfo for RAM details.
     */
    fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    /**
     * Retrieve CPU model/SoC string from Build API with fallback to /proc/cpuinfo or board properties.
     */
    fun getCpuModel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { !it.isNullOrBlank() && it != "unknown" }
                ?: getCpuModelFallback()
        } else {
            getCpuModelFallback()
        }
    }

    private fun getCpuModelFallback(): String {
        val hardware = Build.HARDWARE
        if (!hardware.isNullOrBlank() && hardware != "unknown") return hardware

        val board = Build.BOARD
        if (!board.isNullOrBlank() && board != "unknown") return board

        return try {
            val cpuInfo = File("/proc/cpuinfo").readLines()
            val hardwareLine = cpuInfo.firstOrNull { it.contains("Hardware", ignoreCase = true) }
            hardwareLine?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }
                ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        } catch (_: Exception) {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        }
    }

    /**
     * Get Total RAM in bytes using ActivityManager.
     */
    fun getTotalRam(): Long {
        return getMemoryInfo().totalMem
    }

    /**
     * Get Available RAM in bytes using ActivityManager.
     */
    fun getAvailableRam(): Long {
        return getMemoryInfo().availMem
    }

    /**
     * Get OS Version formatted string from Build.VERSION.
     */
    fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    /**
     * Gathers all core device metrics in a single structured [CoreSystemMetrics] snapshot.
     */
    fun getCoreMetrics(): CoreSystemMetrics {
        val memInfo = getMemoryInfo()
        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availRamGb = memInfo.availMem / (1024f * 1024f * 1024f)

        return CoreSystemMetrics(
            cpuModel = getCpuModel(),
            totalRamBytes = memInfo.totalMem,
            totalRamFormatted = String.format(Locale.getDefault(), "%.2f GB", totalRamGb),
            availableRamBytes = memInfo.availMem,
            availableRamFormatted = String.format(Locale.getDefault(), "%.2f GB", availRamGb),
            osVersion = getOsVersion(),
            sdkInt = Build.VERSION.SDK_INT,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            abis = Build.SUPPORTED_ABIS.toList()
        )
    }
}
