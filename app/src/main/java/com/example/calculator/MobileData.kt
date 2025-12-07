package com.example.calculator

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.telephony.CellInfoLte
import android.telephony.CellInfoGsm
import android.telephony.CellInfoNr
import android.telephony.CellIdentityNr
import android.telephony.CellSignalStrengthNr
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

class MobileData : AppCompatActivity() {
    val TAG = "TelephonyActivity"
    private lateinit var Result: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mobile_data)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Нет разрешений READ_PHONE_STATE или ACCESS_COARSE_LOCATION для получения cell info")
            Toast.makeText(this, "Нет разрешений READ_PHONE_STATE или ACCESS_COARSE_LOCATION для получения cell info", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoList = telephonyManager.allCellInfo
        Log.d(TAG, "${cellInfoList.toString()}")
        val resultText = buildString {
            for ((index, cellInfo) in cellInfoList.withIndex()) {
                append("\nСота ${index + 1}\n")

                when (cellInfo) {
                    is CellInfoLte -> append(getLteInfo(cellInfo))
                    is CellInfoGsm -> append(getGsmInfo(cellInfo))
                    is CellInfoNr -> append(getNrInfo(cellInfo))
                }
            }
        }
        Result = findViewById(R.id.result)
        Result.movementMethod = ScrollingMovementMethod()
        Result.text = resultText
    }
    private fun getLteInfo(cellInfo: CellInfoLte): String {
        return """
            1.CellInfoLte
                1. CellIdentityLte:
                - Band: ${cellInfo.cellIdentity.bands}
                - CellIdentity: ${cellInfo.cellIdentity.ci}
                - EARFCN: ${cellInfo.cellIdentity.earfcn}
                - MCC: ${cellInfo.cellIdentity.mccString}
                - MNC: ${cellInfo.cellIdentity.mncString}
                - PCI: ${cellInfo.cellIdentity.pci}
                - TAC: ${cellInfo.cellIdentity.tac}
            
                2. CellSignalStrengthLte:
                - ASU Level: ${cellInfo.cellSignalStrength.asuLevel}
                - CQI: ${cellInfo.cellSignalStrength.cqi}
                - RSRP: ${cellInfo.cellSignalStrength.rsrp} dBm
                - RSRQ: ${cellInfo.cellSignalStrength.rsrq} dB
                - RSSI: ${cellInfo.cellSignalStrength.rssi} dBm
                - RSSNR: ${cellInfo.cellSignalStrength.rssnr} dB
                - Timing Advance: ${cellInfo.cellSignalStrength.timingAdvance}
                
                Active: ${cellInfo.isRegistered}
        """
    }
    private fun getGsmInfo(cellInfo: CellInfoGsm): String {
        return """
            2. CellInfoGsm
                1. CellIdentityGSM:
                    - CellIdentity: ${cellInfo.cellIdentity.cid}
                    - BSIC: ${cellInfo.cellIdentity.bsic}
                    - ARFCN: ${cellInfo.cellIdentity.arfcn}
                    - LAC: ${cellInfo.cellIdentity.lac}
                    - MCC: ${cellInfo.cellIdentity.mccString}
                    - MNC: ${cellInfo.cellIdentity.mncString}
                    - PSC: ${cellInfo.cellIdentity.psc}
            
                2. CellSignalStrengthGsm:
                    - Dbm: ${cellInfo.cellSignalStrength.dbm} dBm
                    - RSSI: ${cellInfo.cellSignalStrength.rssi} dBm
                    - Timing Advance: ${cellInfo.cellSignalStrength.timingAdvance}
                    
                Active: ${cellInfo.isRegistered}
        """
    }

    private fun getNrInfo(cellInfo: CellInfoNr): String {
        val celi = cellInfo.cellIdentity as CellIdentityNr
        val cels = cellInfo.cellSignalStrength as CellSignalStrengthNr

        return """
            3. CellInfoNr:
                1. CellIdentityNr:
                    - Band: ${celi.bands.firstOrNull()}
                    - NCI: ${celi.nci}
                    - PCI: ${celi.pci}
                    - Nrargcn: ${celi.nrarfcn}
                    - TAC: ${celi.tac}
                    - MCC: ${celi.mccString}
                    - MNC: ${celi.mncString}
            
                2. CellSignalStrengthNr:
                    - SS-RSRP: ${cels.ssRsrp} dBm
                    - SS-RSRQ: ${cels.ssRsrq} dB
                    - SS-SINR: ${cels.ssSinr} dB
                    
                Active: ${cellInfo.isRegistered}
        """
    }
}