package com.example.isowatch

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PemeriksaanService {
    private val tag = "Pemeriksaan Service"

    fun createPemeriksaan(pemeriksaan: Pemeriksaan, onResult: (Pemeriksaan?) -> Unit) {
        val retrofit = ServiceBuilder.buildService(PemeriksaanServiceInterface::class.java)
        retrofit.createPemeriksaan(pemeriksaan).enqueue(
            object : Callback<Pemeriksaan> {
                override fun onResponse(call: Call<Pemeriksaan>, response: Response<Pemeriksaan>) {
                    val newPemeriksaan = response.body()
                    Log.d(tag, "${response}")
                    onResult(newPemeriksaan)
                }
                override fun onFailure(call: Call<Pemeriksaan>, t: Throwable) {
                    Log.e(tag, "create Pemeriksaan error: $t")
                    onResult(null)
                }
            }
        )
    }
}