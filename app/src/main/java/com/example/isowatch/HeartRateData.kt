package com.example.isowatch

class HeartRateData {
    var status = HeartRateStatus.HR_STATUS_NONE
    var hr = 0

    internal constructor() {}
    internal constructor(status: Int, hr: Int) {
        this.status = status
        this.hr = hr
    }
}