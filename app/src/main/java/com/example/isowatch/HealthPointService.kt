package com.example.isowatch

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HealthPointService {
    private val tag = "Health Point Service"

    fun addHealthPoint(healthPoint: HealthPoint, onResult: (HealthPointContainer?) -> Unit) {
        val retrofit = ServiceBuilder.buildService(HealthPointServiceInterface::class.java)
        retrofit.addHealthPoint(healthPoint).enqueue(
            object : Callback<HealthPointContainer> {
                override fun onResponse(
                    call: Call<HealthPointContainer>,
                    response: Response<HealthPointContainer>
                ) {
                    val result = response.body()
                    Log.d(tag, "$result")
                    onResult(result)
                }

                override fun onFailure(call: Call<HealthPointContainer>, t: Throwable) {
                    Log.e(tag, "add Health Point error: $t")
                    onResult(null)
                }
            }
        )
    }
}