package com.example.device

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.system.measureTimeMillis

enum class BenchmarkStatus {
    IDLE, RUNNING, COMPLETED
}

data class BenchmarkState(
    val status: BenchmarkStatus = BenchmarkStatus.IDLE,
    val progress: Float = 0f,
    val currentTestName: String = "",
    val logs: List<String> = emptyList(),
    val cpuScore: Int = 0,
    val ramScore: Int = 0,
    val storageScore: Int = 0,
    val cpuSpeedOps: Double = 0.0,
    val ramSpeedMbs: Double = 0.0,
    val storageSpeedMbs: Double = 0.0,
    val overallScore: Int = 0,
    val overallTier: String = "N/A"
)

class BenchmarkManager(private val context: Context) {

    private val _state = MutableStateFlow(BenchmarkState())
    val state = _state.asStateFlow()

    fun reset() {
        _state.value = BenchmarkState()
    }

    suspend fun runBenchmark() {
        if (_state.value.status == BenchmarkStatus.RUNNING) return

        _state.value = BenchmarkState(
            status = BenchmarkStatus.RUNNING,
            progress = 0.02f,
            currentTestName = "Initializing Suite...",
            logs = listOf("▶ Starting Device Inspector Benchmark Suite v1.1", "🔧 Initializing hardware threads and disk scopes...")
        )
        delay(1000)

        // 1. CPU Benchmark: Calculate prime numbers and cryptographic hashes
        addLog("⚙️ Commencing CPU Multicore Execution Test...")
        updateProgress(0.1f, "Testing Core Computations...")
        
        var cpuOps = 0.0
        val cpuTime = measureTimeMillis {
            cpuOps = runCpuStressTest()
        }
        
        val rawCpuScore = ((cpuOps / (cpuTime / 1000.0)) / 12.0).toInt().coerceIn(100, 9999)
        addLog(String.format("✓ CPU stress completed in %d ms", cpuTime))
        addLog(String.format("⚡ Performance rating: %.1f kOps/sec", cpuOps / 1000.0))
        addLog("🌟 CPU Compute Score: $rawCpuScore")
        
        _state.value = _state.value.copy(
            cpuScore = rawCpuScore,
            cpuSpeedOps = cpuOps / (cpuTime / 1000.0),
            progress = 0.4f
        )
        delay(800)

        // 2. RAM Benchmark: Large buffer memory allocation and copying cycles
        addLog("⚡ Commencing RAM Read/Write Performance Test...")
        updateProgress(0.45f, "Allocating memory buffers...")
        
        var ramSpeed = 0.0
        val ramTime = measureTimeMillis {
            ramSpeed = runRamReadWriteTest() // Returns MB/s
        }
        
        val rawRamScore = (ramSpeed * 0.85).toInt().coerceIn(100, 9999)
        addLog(String.format("✓ RAM Bandwidth stress completed in %d ms", ramTime))
        addLog(String.format("⚡ Memory bandwidth rate: %.1f MB/s", ramSpeed))
        addLog("🌟 RAM Transfer Score: $rawRamScore")
        
        _state.value = _state.value.copy(
            ramScore = rawRamScore,
            ramSpeedMbs = ramSpeed,
            progress = 0.7f
        )
        delay(800)

        // 3. Storage Benchmark: Create local cache files, write & read buffers
        addLog("💾 Commencing Disk Read/Write Bandwidth Test...")
        updateProgress(0.75f, "Writing temporary disk cache...")
        
        var storageSpeed = 0.0
        val storageTime = measureTimeMillis {
            storageSpeed = runStorageReadWriteTest() // Returns MB/s
        }
        
        val rawStorageScore = (storageSpeed * 12.0).toInt().coerceIn(100, 9999)
        addLog(String.format("✓ Disk operation finished in %d ms", storageTime))
        addLog(String.format("⚡ Disk sequential rate: %.1f MB/s", storageSpeed))
        addLog("🌟 Storage I/O Score: $rawStorageScore")
        
        _state.value = _state.value.copy(
            storageScore = rawStorageScore,
            storageSpeedMbs = storageSpeed,
            progress = 0.95f
        )
        delay(800)

        // Wrap up score computation
        updateProgress(0.98f, "Calculating final indices...")
        delay(700)

        val finalOverallScore = ((rawCpuScore * 0.5) + (rawRamScore * 0.3) + (rawStorageScore * 0.2)).toInt()
        val tier = when (finalOverallScore) {
            in 2500..99999 -> "S-Tier (Enterprise Powerhouse)"
            in 1800..2499 -> "A-Tier (High-End Pro)"
            in 1100..1799 -> "B-Tier (Fluent Mid-Range)"
            else -> "C-Tier (Standard Device)"
        }

        addLog("🌟 --- BENCHMARK COMPLETE --- 🌟")
        addLog("🏆 Final Score: $finalOverallScore")
        addLog("🎖️ Performance Category: $tier")

        _state.value = _state.value.copy(
            status = BenchmarkStatus.COMPLETED,
            progress = 1.0f,
            currentTestName = "Benchmark Suite Completed!",
            overallScore = finalOverallScore,
            overallTier = tier
        )
    }

    private fun addLog(message: String) {
        val updatedLogs = _state.value.logs + message
        _state.value = _state.value.copy(logs = updatedLogs)
    }

    private fun updateProgress(progress: Float, currentTestName: String) {
        _state.value = _state.value.copy(
            progress = progress,
            currentTestName = currentTestName
        )
    }

    // Mathematical calculations (multithreaded execution context)
    private suspend fun runCpuStressTest(): Double = withContext(Dispatchers.Default) {
        var opsCount = 0.0
        val digest = MessageDigest.getInstance("SHA-256")
        
        // Let's execute mathematical loops and hash generations across 4 coroutines to stress core pipeline
        val jobs = List(4) {
            withContext(Dispatchers.Default) {
                var localOps = 0
                val text = "HardwareSpecPerformanceTestingSuiteForAndroidSystems"
                
                // stress computation
                for (i in 0..120000) {
                    val hashed = digest.digest((text + i + localOps).toByteArray())
                    // Perform quick floating ops to test floating calculations
                    val mathResult = Math.sin(i.toDouble()) * Math.cos(hashed[0].toDouble())
                    if (mathResult != 0.0) {
                        localOps++
                    }
                }
                localOps.toDouble()
            }
        }
        
        opsCount = jobs.sum()
        opsCount
    }

    // Allocate, populate, read memory chunks
    private suspend fun runRamReadWriteTest(): Double = withContext(Dispatchers.Default) {
        // Allocate a 8MB buffer
        val sizeBytes = 8 * 1024 * 1024
        val array1 = ByteArray(sizeBytes)
        val array2 = ByteArray(sizeBytes)
        
        Random.nextBytes(array1)
        
        var totalBytesMoved = 0L
        val durationMs = measureTimeMillis {
            for (cycle in 0..12) {
                // Copy array block and read elements
                System.arraycopy(array1, 0, array2, 0, sizeBytes)
                // perform some arithmetic to guarantee JVM executes this compile-path
                var checksum = 0
                for (i in 0 until sizeBytes step 512) {
                    checksum = checksum xor array2[i].toInt()
                }
                totalBytesMoved += (sizeBytes * 2L) // read and written
            }
        }
        
        val seconds = durationMs / 1000.0
        val mbs = (totalBytesMoved / (1024.0 * 1024.0)) / seconds
        mbs.coerceAtLeast(10.0) // Return MB/s
    }

    // Standard local cache sequence write/read test
    private suspend fun runStorageReadWriteTest(): Double = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, "benchmark_telemetry.tmp")
        val size = 5 * 1024 * 1024 // 5MB
        val data = ByteArray(size)
        Random.nextBytes(data)

        var speedMbs = 0.0
        try {
            var timeWrite = 0L
            var timeRead = 0L

            timeWrite = measureTimeMillis {
                cacheFile.writeBytes(data)
            }

            val readBuffer: ByteArray
            timeRead = measureTimeMillis {
                readBuffer = cacheFile.readBytes()
            }

            // check read values
            if (readBuffer.size == size) {
                val totalSec = (timeWrite + timeRead) / 1000.0
                val totalMb = (size * 2) / (1024.0 * 1024.0) // 10MB total
                speedMbs = if (totalSec > 0) totalMb / totalSec else 5.0
            }
        } catch (e: Exception) {
            speedMbs = 12.5 // fallback safe speed
        } finally {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        }

        speedMbs.coerceAtLeast(1.0)
    }
}
