package com.example.isowatch

import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker.TrackerError
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class HeartRateListener internal constructor() : BaseListener() {
    private val tag = "Heart Rate Listener"

    init {
        val trackerEventListener: TrackerEventListener = object : TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                for (dataPoint in list) {
                    readValuesFromDataPoint(dataPoint)
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

    fun readValuesFromDataPoint(dataPoint: DataPoint) {
        val hrData = HeartRateData()
        hrData.status = dataPoint.getValue(ValueKey.HeartRateSet.STATUS)
        hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)
        TrackerDataNotifier.instance?.notifyHeartRateTrackerObservers(hrData)
        Log.d(tag, dataPoint.toString())
    }
}