package com.example.isowatch

interface TrackerDataObserver {
    fun onHeartRateTrackerDataChanged(hrData: HeartRateData)

    fun onSpO2TrackerDataChanged(status: Int, spO2Value: Int)

    fun onError(errorResourceId: Int)
}