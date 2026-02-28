package com.example.calculator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NetworkMap : AppCompatActivity() {
    private lateinit var tvStatusLoger: TextView
    private lateinit var toggleLog: ToggleButton

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_map)

        tvStatusLoger = findViewById(R.id.tvStatusLoger)
        toggleLog = findViewById(R.id.toggleButton)

        toggleLog.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasAllPermissions()) {
                    startLogService()
                } else {
                    toggleLog.isChecked = false
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 101)
                }
            } else {
                stopLogService()
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startLogService() {
        val intent = Intent(this, NetworkLoggingService::class.java)
        ContextCompat.startForegroundService(this, intent)
        tvStatusLoger.text = "Статус: Логирование запущено..."
    }

    private fun stopLogService() {
        val intent = Intent(this, NetworkLoggingService::class.java)
        stopService(intent)
        tvStatusLoger.text = "Статус: Остановлено"
    }
}