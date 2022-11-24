package com.example.isowatch

import android.os.Handler
import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener

public open class BaseListener {
    private val tag = "BaseListener"

    private lateinit var handler: Handler
    private lateinit var healthTracker: HealthTracker
    private var isHandlerRunning = false

    private lateinit var trackerEventListener: TrackerEventListener

    public fun setHealthTracker(tracker: HealthTracker) {
        healthTracker = tracker
    }

    public fun setHandler(handler: Handler) {
        this.handler = handler
    }

    public fun setHandlerRunning(handlerRunning: Boolean) {
        isHandlerRunning = handlerRunning
    }

    public fun setTrackerEventListener(tracker: TrackerEventListener) {
        trackerEventListener = tracker
    }

    public fun startTracker() {
        Log.i(tag, "startTracker called ")
        Log.d(tag, "healthTracker: $healthTracker")
        Log.d(tag, "trackerEventListener: $trackerEventListener")
        if (!isHandlerRunning) {
            handler.post {
                healthTracker.setEventListener(trackerEventListener)
                setHandlerRunning(true)
            }
        }
    }

    public fun stopTracker() {
        Log.i(tag, "stopTracker called ")
        Log.d(tag, "healthTracker: $healthTracker")
        Log.d(tag, "trackerEventListener: $trackerEventListener")
        if (isHandlerRunning) {
            healthTracker.unsetEventListener()
            setHandlerRunning(false)

            handler.removeCallbacksAndMessages(null)
        }
    }
}