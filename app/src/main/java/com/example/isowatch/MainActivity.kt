package com.example.isowatch

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.isowatch.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : Activity() {

    private val tag = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var idPemeriksaan: String? = null
    private var isLoading = false
    private var isError = false
    private val tanggalMulai: String = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if ((ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.BodySensors)
            ) == PackageManager.PERMISSION_DENIED)
            ||
            (ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.Internet),
            ) == PackageManager.PERMISSION_DENIED)
        ) requestPermissions(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.INTERNET,
            ), 0
        )

        binding.root.findViewById<Button>(R.id.buttonCreate).setOnClickListener {
            if (idPemeriksaan == null) {
                Log.d("MainActivity", "Creating Pemeriksaan")
                // make a Create Pemeriksaan API Call
                createPemeriksaan()
            }
            Toast.makeText(
                applicationContext,
                R.string.CreateMessage,
                Toast.LENGTH_SHORT
            )
        }

        binding.root.findViewById<Button>(R.id.buttonStart).setOnClickListener {
            if (idPemeriksaan != null) {
                // Go to MonitoringActiity
                val context = binding.root.context

                val intent = Intent(context, MonitoringActivity::class.java)
                intent.putExtra("idPemeriksaan", idPemeriksaan)
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    applicationContext,
                    R.string.CreateNotice,
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    fun createPemeriksaan() {
        val pemeriksaanService = PemeriksaanService()
        val pemeriksaan = Pemeriksaan(
            _id = null,
            idPemeriksaan = null,
            idAdmin = emptyArray<String>(),
            tanggalMulai = tanggalMulai,
            __v = null,
        )

        pemeriksaanService.createPemeriksaan(pemeriksaan) {
            if (it?.idPemeriksaan != null) {
                idPemeriksaan = it.idPemeriksaan
            } else {
                Log.e(tag,"Error creating Pemeriksaan")
            }
            Log.i(tag,"result: $it")
        }
    }
}