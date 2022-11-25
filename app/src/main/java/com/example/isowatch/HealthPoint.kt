package com.example.isowatch

data class HealthPoint(
    val _id: String?,
    val idPemeriksaan: String,
    val timestamp: String,
    val heartRate: Int,
    val diastolicBloodPressure: Int,
    val sistolicBloodPressure: Int,
    val bloodOxygen: Int,
    val __v: Int?,
)
