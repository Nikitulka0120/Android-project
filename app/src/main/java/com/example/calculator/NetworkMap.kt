package com.example.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.*
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File

class NetworkMap : AppCompatActivity(), LocationListener {
    private val LOG_TAG: String = "MapBuilder_ACTIVITY"
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var tvStatusLoger: TextView
    private lateinit var toggleLog: ToggleButton

    private lateinit var handler: Handler
    private var zmqContext: ZContext? = null
    private val serverAddress = "tcp://192.168.0.14:7777"

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_network_map)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tvStatusLoger = findViewById(R.id.tvStatusLoger)
        toggleLog = findViewById(R.id.toggleButton)

        tvStatusLoger.movementMethod = ScrollingMovementMethod()
        handler = Handler(Looper.getMainLooper())
        zmqContext = ZContext()

        toggleLog.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkAllPermissions()) {
                    tvStatusLoger.append("\n--- Логирование запущено ---\n")
                    startLogging()
                } else {
                    toggleLog.isChecked = false
                    requestPermissions()
                }
            } else {
                tvStatusLoger.append("\n--- Остановлено ---\n")
                stopLocationUpdates()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startLogging() {
        if (!isLocationEnabled()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, this)
            }
        } catch (e: SecurityException) {
            logToUi("Security Error: ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        val networkData = getCellularData()

        val jsonPayload = JSONObject().apply {
            put("lat", location.latitude)
            put("lon", location.longitude)
            put("alt", location.altitude)
            put("time", location.time)
            put("network", networkData)
        }.toString()

        saveToFile(jsonPayload)
        Thread { sendZmqData(jsonPayload) }.start()
    }

    private fun getCellularData(): String {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Permission Denied"
        }

        return try {
            val cellInfoList = telephonyManager.allCellInfo
            if (cellInfoList.isNullOrEmpty()) return "No Cell Info"

            val result = StringBuilder()
            for (cellInfo in cellInfoList) {
                if (cellInfo.isRegistered) {
                    when (cellInfo) {
                        is CellInfoLte -> result.append("LTE: PCI:${cellInfo.cellIdentity.pci} RSRP:${cellInfo.cellSignalStrength.rsrp} ")
                        is CellInfoGsm -> result.append("GSM: CID:${cellInfo.cellIdentity.cid} RSSI:${cellInfo.cellSignalStrength.rssi} ")
                        is CellInfoNr -> {
                            val celi = cellInfo.cellIdentity as CellIdentityNr
                            val cels = cellInfo.cellSignalStrength as CellSignalStrengthNr
                            result.append("NR: NCI:${celi.nci} RSRP:${cels.ssRsrp} ")
                        }
                    }
                }
            }
            result.toString().trim().ifEmpty { "No Registered Towers" }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun checkAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    private fun logToUi(message: String) {
        handler.post { tvStatusLoger.append("$message\n") }
    }

        private fun sendZmqData(data: String) {
            try {
                Log.d(LOG_TAG, "ZMQ: Попытка отправки данных...")
                zmqContext?.let { context ->
                    val socket = context.createSocket(SocketType.REQ)
                    socket.receiveTimeOut = 2000
                    socket.sendTimeOut = 2000
                    socket.linger = 0
                    socket.connect(serverAddress)
                    val sent = socket.send(data.toByteArray(ZMQ.CHARSET), 0)
                    if (sent) {
                        Log.d(LOG_TAG, "ZMQ: Данные отправлены, ожидание ответа...")
                        val replyBytes = socket.recv(0)
                        if (replyBytes != null) {
                            val reply = String(replyBytes, ZMQ.CHARSET)
                            Log.d(LOG_TAG, "ZMQ: Получен ответ: $reply")
                            logToUi("SERVER: $reply")
                        } else {
                            Log.w(LOG_TAG, "ZMQ: Ответ не получен (Timeout)")
                            logToUi("ZMQ: No response from server")
                        }
                    } else {
                        Log.e(LOG_TAG, "ZMQ: Не удалось отправить данные")
                    }
                    socket.close()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "ZMQ ERROR: ${e.message}")
                logToUi("ZMQ ERROR: ${e.message}")
            }
        }

    private fun saveToFile(data: String) {
        try {
            val exfile = File(getExternalFilesDir(null), "locations_network.json")
            exfile.appendText(data + "\n")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "File error", e)
        }
    }

    private fun isLocationEnabled() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    override fun onDestroy() {
        super.onDestroy()
        zmqContext?.close()
    }
}