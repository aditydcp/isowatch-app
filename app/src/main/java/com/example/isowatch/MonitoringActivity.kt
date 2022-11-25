package com.example.isowatch

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.isowatch.databinding.ActivityMonitoringBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.samsung.android.service.health.tracking.HealthTrackerException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

class MonitoringActivity: Activity() {
    private val tag = "Monitoring Activity"

    private val MEASUREMENT_DURATION = 30000
    private val MEASUREMENT_TICK = 250

    private var isMeasurementRunning = AtomicBoolean(false)
    lateinit var uiUpdateThread: Thread
    private lateinit var connectionManager: ConnectionManager
    private lateinit var heartRateListener: HeartRateListener
    private lateinit var spO2Listener: SpO2Listener
    private var idPemeriksaan = "0"
    private var connected = false
    private var permissionGranted = false
    private var previousSpO2Status = SpO2Status.INITIAL_STATUS
    private var heartRateDataLast = HeartRateData()
    private var heartRateDataStore = mutableListOf<Int>()
    private var spO2DataStore = 0
    private lateinit var timestamp: String
    private lateinit var txtHeartRate: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtSpo2: TextView
    private lateinit var txtId: TextView
    private lateinit var butStart: Button
    private lateinit var measurementProgress: CircularProgressIndicator
    private val countDownTimer: CountDownTimer = object : CountDownTimer(
        MEASUREMENT_DURATION.toLong(),
        MEASUREMENT_TICK.toLong()
    ) {
        override fun onTick(timeLeft: Long) {
            if (isMeasurementRunning.get()) {
                runOnUiThread {
                    measurementProgress.setProgress(
                        measurementProgress.progress + 1,
                        true
                    )
                }
            } else cancel()
        }

        override fun onFinish() {
            if (!isMeasurementRunning.get()) return
            Log.i(tag, "Failed measurement")
            runOnUiThread {
                txtStatus.setText(R.string.MeasurementFailed)
                txtStatus.invalidate()
                txtSpo2.setText(R.string.SpO2DefaultValue)
                txtSpo2.invalidate()
                butStart.setText(R.string.StartLabel)
                measurementProgress.progress = 0
                measurementProgress.invalidate()
            }
            spO2Listener.stopTracker()
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            isMeasurementRunning.set(false)
        }
    }
    val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {

        override fun onHeartRateTrackerDataChanged(hrData: HeartRateData) {
            this@MonitoringActivity.runOnUiThread(Runnable {
                heartRateDataLast = hrData
                Log.i(tag, "HR Status: " + hrData.status)
                if (hrData.status === HeartRateStatus.HR_STATUS_FIND_HR) {
                    txtHeartRate.text = java.lang.String.valueOf(hrData.hr)
                    Log.i(tag, "HR: " + hrData.hr)
                } else {
                    txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
                }
                // store values when calculating
                if (previousSpO2Status == SpO2Status.CALCULATING) {
                    if (hrData.status === HeartRateStatus.HR_STATUS_FIND_HR) {
                        heartRateDataStore.add(hrData.hr)
                    }
                }
            })
        }

        override fun onSpO2TrackerDataChanged(status: Int, spO2Value: Int) {
            if (status == previousSpO2Status) {
                return
            }
            previousSpO2Status = status
            when (status) {
                SpO2Status.CALCULATING -> {
                    Log.i(tag, "Calculating measurement")
                    runOnUiThread {
                        txtStatus.setText(R.string.StatusCalculating)
                        txtStatus.invalidate()
                    }
                }
                SpO2Status.DEVICE_MOVING -> {
                    Log.i(tag, "Device is moving")
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.StatusDeviceMoving,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                SpO2Status.LOW_SIGNAL -> {
                    Log.i(tag, "Low signal quality")
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.StatusLowSignal,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                SpO2Status.MEASUREMENT_COMPLETED -> {
                    Log.i(tag, "Measurement completed")
                    isMeasurementRunning.set(false)
                    spO2Listener.stopTracker()
                    runOnUiThread {
                        txtStatus.setText(R.string.StatusCompleted)
                        txtStatus.invalidate()
                        txtSpo2.text = spO2Value.toString()
                        txtSpo2.invalidate()
                        Log.i(tag, "SpO2 measured: $spO2Value")
                        butStart.setText(R.string.StartLabel)
                        measurementProgress.setProgress(measurementProgress.max, true)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    spO2DataStore = spO2Value
                    sendHealthPoint()
                }
            }
        }

        override fun onError(errorResourceId: Int) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    getString(errorResourceId),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private val connectionObserver: ConnectionObserver = object : ConnectionObserver {
        override fun onConnectionResult(stringResourceId: Int) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext, getString(stringResourceId), Toast.LENGTH_LONG
                ).show()
            }
            if (stringResourceId != R.string.ConnectedToHs) {
                finish()
            }

            connected = true
            TrackerDataNotifier.instance?.addObserver(trackerDataObserver)

            spO2Listener = SpO2Listener()
            heartRateListener = HeartRateListener()

            connectionManager.initSpO2(spO2Listener)
            connectionManager.initHeartRate(heartRateListener)

            heartRateListener.startTracker()
        }

        override fun onError(e: HealthTrackerException?) {
            if (e != null) {
                if (e.errorCode == HealthTrackerException.OLD_PLATFORM_VERSION || e.errorCode == HealthTrackerException.PACKAGE_NOT_INSTALLED) runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.HealthPlatformVersionIsOutdated),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            if (e != null) {
                if (e.hasResolution()) {
                    e.resolve(this@MonitoringActivity)
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext, getString(R.string.ConnectionError), Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e(tag, "Could not connect to Health Tracking Service: " + e.message)
                }
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tag, "I'm here!")

        val binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txtHeartRate = binding.txtHeartRate
        txtStatus = binding.txtStatus
        txtSpo2 = binding.txtSpO2
        txtId = binding.noIdPemeriksaan
        butStart = binding.butStart
        measurementProgress = binding.progressBar
        adjustProgressBar(measurementProgress)

        if ((ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.BodySensors)
            ) == PackageManager.PERMISSION_DENIED)
            ||
            (ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.Internet),
            ) == PackageManager.PERMISSION_DENIED)
        ) requestPermissions(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.INTERNET,
            ), 0
        ) else {
            permissionGranted = true
            createConnectionManager()
        }

        idPemeriksaan = intent?.extras?.getString("idPemeriksaan").toString()
        if(idPemeriksaan.isNotEmpty()){
            runOnUiThread {
                txtId.text = idPemeriksaan
            }
        }
    }

    override fun onPause() {
        Log.d(tag, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
        heartRateListener.stopTracker()
        spO2Listener.stopTracker()
        TrackerDataNotifier.instance?.removeObserver(trackerDataObserver)
        connectionManager.disconnect()
    }

    private fun createConnectionManager() {
        try {
            connectionManager = ConnectionManager(connectionObserver)
            connectionManager.connect(applicationContext)
        } catch (t: Throwable) {
            Log.e(tag, t.message!!)
        }
    }

    private fun adjustProgressBar(progressBar: CircularProgressIndicator) {
        val displayMetrics = this.resources.displayMetrics
        val pxWidth = displayMetrics.widthPixels
        val padding = 1
        progressBar.setPadding(padding, padding, padding, padding)
        val trackThickness = progressBar.trackThickness
        val progressBarSize = pxWidth - trackThickness - 2 * padding
        progressBar.indicatorSize = progressBarSize
    }

    fun performMeasurement(view: View?) {
        if (isPermissionsOrConnectionInvalid()) {
            return
        }
        if (!isMeasurementRunning.get()) {
            previousSpO2Status = SpO2Status.INITIAL_STATUS
            butStart.setText(R.string.StopLabel)
            txtSpo2.setText(R.string.SpO2DefaultValue)
            measurementProgress.progress = 0
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            spO2Listener.startTracker()
            isMeasurementRunning.set(true)
            uiUpdateThread = Thread { countDownTimer.start() }
            uiUpdateThread.start()
        } else {
            butStart.isEnabled = false
            isMeasurementRunning.set(false)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            spO2Listener.stopTracker()
            val progressHandler = Handler(Looper.getMainLooper())
            progressHandler.postDelayed(
                {
                    runOnUiThread {
                        butStart.setText(R.string.StartLabel)
                        txtStatus.setText(R.string.StatusDefaultValue)
                        measurementProgress.progress = 0
                        butStart.isEnabled = true
                    }
                }, (MEASUREMENT_TICK * 2).toLong()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            permissionGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.PermissionDeniedRationale),
                        Toast.LENGTH_LONG
                    ).show()
                    permissionGranted = false
                    break
                }
            }
            if (permissionGranted) {
                createConnectionManager()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isPermissionsOrConnectionInvalid(): Boolean {
        if ((ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.BodySensors)
            ) == PackageManager.PERMISSION_DENIED)
            ||
            (ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.Internet),
            ) == PackageManager.PERMISSION_DENIED)
        ) requestPermissions(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.INTERNET,
            ), 0
        )
        if (!permissionGranted) {
            Log.i(tag, "Could not get permissions. Terminating measurement")
            return true
        }
        if (!connected) {
            Toast.makeText(
                applicationContext,
                getString(R.string.ConnectionError),
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false
    }

    private fun sendHealthPoint() {
        Log.d(tag, "entering sendHealthPoint()")
        // get current time
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).toString()

        // get avg heart rate in the measured time span
        var totalCapture = 0
        var avgHeartRate = 0
        for (data in heartRateDataStore) {
            totalCapture += data
        }
        if (heartRateDataStore.size > 0) avgHeartRate = totalCapture / heartRateDataStore.size

        // call the service
        val healthPointService = HealthPointService()
        val healthPoint = HealthPoint(
            _id = null,
            idPemeriksaan = idPemeriksaan,
            timestamp = timestamp,
            heartRate = avgHeartRate,
            diastolicBloodPressure = 0,
            sistolicBloodPressure = 0,
            bloodOxygen = spO2DataStore,
            __v = null,
        )

        healthPointService.addHealthPoint(healthPoint) {
            if (it?.result != null) {
                Log.i(tag, "Data berhasil terkirim!")
            } else {
                Log.e(tag,"Error creating Pemeriksaan")
            }
            Log.i(tag,"result: $it")
        }

        heartRateDataStore.clear()
        spO2DataStore = 0
    }
}