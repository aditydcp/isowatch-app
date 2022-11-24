package com.example.isowatch

import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker.TrackerError
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class SpO2Listener internal constructor() : BaseListener() {
    private val tag = "SpO2 Listener"

    init {
        val trackerEventListener: TrackerEventListener = object : TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                for (data in list) {
                    updateSpo2(data)
                }
            }

            override fun onFlushCompleted() {
                Log.i(tag, " onFlushCompleted called")
            }

            override fun onError(trackerError: TrackerError) {
                Log.e(tag, " onError called: $trackerError")
                setHandlerRunning(false)
                if (trackerError == TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.instance?.notifyError(R.string.NoPermission)
                }
                if (trackerError == TrackerError.SDK_POLICY_ERROR) {
                    TrackerDataNotifier.instance?.notifyError(R.string.SdkPolicyError)
                }
            }
        }
        setTrackerEventListener(trackerEventListener)
    }

    fun updateSpo2(dataPoint: DataPoint) {
        val status = dataPoint.getValue(ValueKey.SpO2Set.STATUS)
        var spo2Value = 0
        spo2Value = dataPoint.getValue(ValueKey.SpO2Set.SPO2)
        TrackerDataNotifier.instance?.notifySpO2TrackerObservers(status, spo2Value)
        Log.d(tag, dataPoint.toString())
    }
}