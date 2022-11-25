package com.example.isowatch

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HealthPointServiceInterface {
    @Headers("Content-Type: application/json")
    @POST("/patient/pemeriksaan/healthpoint")
    fun addHealthPoint(@Body healthPoint: HealthPoint): Call<HealthPointContainer>
}