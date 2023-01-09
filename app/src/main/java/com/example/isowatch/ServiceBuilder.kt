package com.example.isowatch

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://isowatch-web.up.railway.app")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    fun<T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}