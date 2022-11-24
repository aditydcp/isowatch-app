package com.example.isowatch

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PemeriksaanServiceInterface {
    @Headers("Content-Type: application/json")
    @POST("/patient/pemeriksaan/add")
    fun createPemeriksaan(@Body pemeriksaan: Pemeriksaan): Call<Pemeriksaan>
}