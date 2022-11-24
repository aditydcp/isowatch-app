package com.example.isowatch

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.HealthTrackerType

class ConnectionManager(observer: ConnectionObserver) {
    private val tag = "Connection Manager"
    private var connectionObserver: ConnectionObserver = observer
    private lateinit var healthTrackingService: HealthTrackingService
    private val connectionListener: ConnectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            Log.i(tag, "Connected")
            connectionObserver!!.onConnectionResult(R.string.ConnectedToHs)
            if (!isSpO2Available(healthTrackingService)) {
                Log.i(tag, "Device does not support SpO2 tracking")
                connectionObserver.onConnectionResult(R.string.NoSpo2Support)
            }
            if (!isHeartRateAvailable(healthTrackingService)) {
                Log.i(tag, "Device does not support Heart Rate tracking")
                connectionObserver.onConnectionResult(R.string.NoHrSupport)
            }
        }

        override fun onConnectionEnded() {
            Log.i(tag, "Disconnected")
        }

        override fun onConnectionFailed(e: HealthTrackerException) {
            connectionObserver!!.onError(e)
        }
    }

    fun connect(context: Context?) {
        healthTrackingService = HealthTrackingService(connectionListener, context)
        healthTrackingService!!.connectService()
    }

    fun disconnect() {
        healthTrackingService?.disconnectService()
    }

    fun initSpO2(spO2Listener: SpO2Listener) {
        val healthTracker = healthTrackingService.getHealthTracker(HealthTrackerType.SPO2)
        spO2Listener.setHealthTracker(healthTracker)
        setHandlerForBaseListener(spO2Listener)
    }

    fun initHeartRate(heartRateListener: HeartRateListener) {
        val healthTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE)
        heartRateListener.setHealthTracker(healthTracker)
        setHandlerForBaseListener(heartRateListener)
    }

    private fun setHandlerForBaseListener(baseListener: BaseListener) {
        baseListener.setHandler(Handler(Looper.getMainLooper()))
    }

    private fun isSpO2Available(healthTrackingService: HealthTrackingService): Boolean {
        val availableTrackers = healthTrackingService.trackingCapability.supportHealthTrackerTypes
        return availableTrackers.contains(HealthTrackerType.SPO2)
    }

    private fun isHeartRateAvailable(healthTrackingService: HealthTrackingService): Boolean {
        val availableTrackers = healthTrackingService.trackingCapability.supportHealthTrackerTypes
        return availableTrackers.contains(HealthTrackerType.HEART_RATE)
    }
}