package com.example.isowatch

import com.samsung.android.service.health.tracking.HealthTrackerException

public interface ConnectionObserver {
    abstract fun onConnectionResult(stringResourceId: Int)

    abstract fun onError(e: HealthTrackerException?)
}