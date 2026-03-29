package com.example.calculator

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.concurrent.Executors
import java.io.File

class NetworkLoggingService : Service(), LocationListener {

    private val CHANNEL_ID = "NetworkMappingChannel"
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private var zmqContext: ZContext? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val serverAddress = "tcp://192.168.0.14:7777"

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        zmqContext = ZContext()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        startTracking()
        return START_STICKY
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.requestCellInfoUpdate(executor, object : TelephonyManager.CellInfoCallback() {
                override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                    processAndSend(location, cellInfo)
                }
            })
        } else {
            processAndSend(location, telephonyManager.allCellInfo)
        }
    }
    private fun saveToFile(data: String) {
        try {
            val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) {
                dir.mkdirs()
            }
            val exfile = File(dir, "locations_network.json")
            Log.d("FileCheck", "Запись в файл: ${exfile.absolutePath}")
            exfile.appendText(data + "\n")
        } catch (e: Exception) {
            Log.e("FileError", "Ошибка записи: ${e.message}")
        }
    }

    private fun processAndSend(location: Location, cellInfoList: List<CellInfo>?) {
        val payload = JSONObject().apply {
            put("lat", location.latitude)
            put("lon", location.longitude)
            put("alt", location.altitude)
            put("acc", location.accuracy)
            put("time", System.currentTimeMillis())
            put("cell_data", extractCellDetails(cellInfoList))
        }.toString()

        executor.execute {
            saveToFile(payload)
            sendZmqData(payload)
        }
    }

    private fun extractCellDetails(cellInfoList: List<CellInfo>?): JSONObject {
        val details = JSONObject()
        val info = cellInfoList?.firstOrNull { it.isRegistered } ?: return details.put("error", "No Cell Info")
        try {
            when (info) {
                is CellInfoLte -> {
                    val id = info.cellIdentity
                    val sig = info.cellSignalStrength
                    details.put("type", "LTE")

                    details.put("identity", JSONObject().apply {
                        put("band",  id.bands.joinToString())
                        put("ci", id.ci)
                        put("earfcn", id.earfcn)
                        put("mcc", id.mccString)
                        put("mnc", id.mncString)
                        put("pci", id.pci)
                        put("tac", id.tac)
                    })

                    details.put("signal", JSONObject().apply {
                        put("asuLevel", sig.asuLevel)
                        put("cqi", sig.cqi)
                        put("rsrp", sig.rsrp)
                        put("rsrq", sig.rsrq)
                        put("rssi", sig.rssi)
                        put("rssnr", sig.rssnr)
                        put("timingAdvance", sig.timingAdvance)
                    })
                }

                is CellInfoGsm -> {
                    val id = info.cellIdentity
                    val sig = info.cellSignalStrength
                    details.put("type", "GSM")

                    details.put("identity", JSONObject().apply {
                        put("cid", id.cid)
                        put("bsic", id.bsic)
                        put("arfcn", id.arfcn)
                        put("lac", id.lac)
                        put("mcc", id.mccString)
                        put("mnc", id.mncString)
                        put("psc", id.psc)
                    })

                    details.put("signal", JSONObject().apply {
                        put("dbm", sig.dbm)
                        put("rssi", sig.asuLevel)
                        put("timingAdvance", sig.timingAdvance)
                    })
                }

                is CellInfoNr -> {
                    val id = info.cellIdentity as CellIdentityNr
                    val sig = info.cellSignalStrength as CellSignalStrengthNr
                    details.put("type", "NR")

                    details.put("identity", JSONObject().apply {
                        put("band", id.bands.joinToString())
                        put("nci", id.nci)
                        put("pci", id.pci)
                        put("nrarfcn", id.nrarfcn)
                        put("tac", id.tac)
                        put("mcc", id.mccString)
                        put("mnc", id.mncString)
                    })

                    details.put("signal", JSONObject().apply {
                        put("ssRsrp", sig.ssRsrp)
                        put("ssRsrq", sig.ssRsrq)
                        put("ssSinr", sig.ssSinr)
                    })
                }
            }
        } catch (e: Exception) {
            details.put("parsing_error", e.message)
        }

        return details
    }

    private fun sendZmqData(data: String) {
        try {
            zmqContext?.let { context ->
                val socket = context.createSocket(SocketType.REQ)
                socket.receiveTimeOut = 2000
                socket.sendTimeOut = 2000

                Log.d("ZMQ_LOG", "Попытка подключения к $serverAddress...")
                socket.connect(serverAddress)

                Log.d("ZMQ_LOG", "Отправка пакета (${data.length} байт)...")
                val sent = socket.send(data.toByteArray(ZMQ.CHARSET), 0)

                if (sent) {
                    val response = socket.recv(0)
                    if (response != null) {
                        val respStr = String(response, ZMQ.CHARSET)
                        Log.i("ZMQ_LOG", "Данные доставлены успешно! Ответ сервера: $respStr")
                    } else {
                        Log.w("ZMQ_LOG", "Данные отправлены, но сервер не ответил (Timeout)")
                    }
                } else {
                    Log.e("ZMQ_LOG", "Ошибка: не удалось поместить данные в очередь отправки")
                }

                socket.close()
            }
        } catch (e: Exception) {
            Log.e("ZMQ_LOG", "Критическая ошибка ZMQ: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        val chan = NotificationChannel(CHANNEL_ID, "NetLog", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Network Logging")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationManager.removeUpdates(this)
        zmqContext?.close()
        executor.shutdown()
        super.onDestroy()
    }
}