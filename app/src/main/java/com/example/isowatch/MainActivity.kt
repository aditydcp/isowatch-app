package com.example.isowatch

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.isowatch.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private val tag = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.findViewById<Button>(R.id.buttonStart).setOnClickListener {
            Log.d("MainActivity", "Button Clicked")
            val context = binding.root.context

            val intent = Intent(context, MonitoringActivity::class.java)
            intent.putExtra("idPemeriksaan", "02")
            context.startActivity(intent)
        }

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
    }
}