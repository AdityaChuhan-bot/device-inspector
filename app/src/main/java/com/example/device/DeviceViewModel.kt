package com.example.device

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val modules = DeviceModules(context)
    private val benchmarkManager = BenchmarkManager(context)

    // --- Active states ---
    private val _deviceSummary = MutableStateFlow(modules.getDeviceSummary())
    val deviceSummary = _deviceSummary.asStateFlow()

    private val _cpuInfo = MutableStateFlow(modules.getCpuInfo())
    val cpuInfo = _cpuInfo.asStateFlow()

    private val _ramInfo = MutableStateFlow(modules.getRamInfo())
    val ramInfo = _ramInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(modules.getBatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow(modules.getStorageInfo())
    val storageInfo = _storageInfo.asStateFlow()

    private val _networkInfo = MutableStateFlow(modules.getNetworkInfo())
    val networkInfo = _networkInfo.asStateFlow()

    private val _displayInfo = MutableStateFlow(modules.getDisplayInfo())
    val displayInfo = _displayInfo.asStateFlow()

    private val _sensorList = MutableStateFlow(modules.getSensorList())
    val sensorList = _sensorList.asStateFlow()

    // --- Benchmark states ---
    val benchmarkState = benchmarkManager.state

    // --- Live Sensor Streaming ---
    private val _activeSensorId = MutableStateFlow<Int?>(null)
    val activeSensorId = _activeSensorId.asStateFlow()

    private val _liveSensorValues = MutableStateFlow<List<Float>>(listOf(0f, 0f, 0f))
    val liveSensorValues = _liveSensorValues.asStateFlow()

    // --- Navigation switcher ---
    private val _currentScreen = MutableStateFlow("splash") // splash, dashboard, cpu, ram, battery, storage, network, sensors, display, benchmark
    val currentScreen = _currentScreen.asStateFlow()

    private var pollingJob: Job? = null
    private var sensorManager: SensorManager? = null
    private var sensorListener: SensorEventListener? = null

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        startRealTimePolling()
    }

    private fun startRealTimePolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // Give 1.5 seconds for splash screen
            delay(1800)
            _currentScreen.value = "dashboard"

            while (true) {
                try {
                    _cpuInfo.value = modules.getCpuInfo()
                    _ramInfo.value = modules.getRamInfo()
                    _batteryInfo.value = modules.getBatteryInfo()
                    _networkInfo.value = modules.getNetworkInfo()
                } catch (e: Exception) {
                    // Fail gracefully
                }
                delay(1000)
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        // Stop sensor loggers whenever switching screens to save energy! Excellent optimization metric!
        if (screen != "sensors") {
            stopSensorLogging()
        }
    }

    // --- Sensor telemetry management (optimized polling) ---

    fun startSensorLogging(sensorType: Int, sensorId: Int) {
        stopSensorLogging() // stop any existing listener
        _activeSensorId.value = sensorId
        _liveSensorValues.value = listOf(0f, 0f, 0f)

        val sm = sensorManager ?: return
        val targetSensor = sm.getDefaultSensor(sensorType) ?: return

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val vals = event.values.toList()
                    _liveSensorValues.value = if (vals.size >= 3) {
                        listOf(vals[0], vals[1], vals[2])
                    } else if (vals.isNotEmpty()) {
                        vals + List(3 - vals.size) { 0f }
                    } else {
                        listOf(0f, 0f, 0f)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not required for dashboard displays
            }
        }

        // DELAY_NORMAL is completely fine and respects resource limits
        sm.registerListener(sensorListener, targetSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopSensorLogging() {
        _activeSensorId.value = null
        val sm = sensorManager
        val listener = sensorListener
        if (sm != null && listener != null) {
            sm.unregisterListener(listener)
        }
        sensorListener = null
    }

    // --- Benchmark trigger ---

    fun startBenchmarkTest() {
        viewModelScope.launch {
            benchmarkManager.runBenchmark()
        }
    }

    fun resetBenchmarkTest() {
        benchmarkManager.reset()
    }

    // Refresh layout data manually if desired
    fun refreshStaticData() {
        _deviceSummary.value = modules.getDeviceSummary()
        _storageInfo.value = modules.getStorageInfo()
        _displayInfo.value = modules.getDisplayInfo()
        _sensorList.value = modules.getSensorList()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        stopSensorLogging()
    }
}
