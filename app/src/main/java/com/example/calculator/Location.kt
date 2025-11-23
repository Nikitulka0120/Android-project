package com.example.calculator
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.provider.Settings
import android.util.Log
import java.io.File


class CurrentLoc(
    val Latitute: Double,
    val Longitute: Double,
    val Altitute: Double,
    val time: Long
){
    fun saveInFile(file: File, context: Context){
        if (!file.exists()) {
            Toast.makeText(context, "Попытка создания файл", Toast.LENGTH_SHORT).show()
            file.createNewFile()
            Toast.makeText(context, "Файл создан", Toast.LENGTH_SHORT).show()
        }
        try {
            file.appendText("""{"lat":$Latitute,"lon":$Longitute,"alt":$Altitute,"time":$time}""" + "\n")
        } catch (e: Exception) {
            Log.e("LocationData", "Error saving to file", e)
            Toast.makeText(context, "Error saving location", Toast.LENGTH_SHORT).show()
            }
        }
    }

class Location : LocationListener, AppCompatActivity() {
    val value: Int = 0
    val LOG_TAG: String = "LOCATION_ACTIVITY"
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION= 100
    }

    private lateinit var locationManager: LocationManager
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        tvLat = findViewById(R.id.TextLat) as TextView
        tvLon = findViewById(R.id.TextLon) as TextView
        tvAlt = findViewById(R.id.TextAlt) as TextView
        tvTime = findViewById(R.id.textCurTime) as TextView
    }

    override fun onResume() {
        super.onResume()
        updateCurrentLocation()
    }

    private fun updateCurrentLocation(){

        if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )

            } else{
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "location permission is not allowed");
            tvLat.setText("Permission is not granted")
            tvLon.setText("Permission is not granted")
            tvAlt.setText("Permission is not granted")
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        if (!android.os.Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        }
        Log.w(LOG_TAG, "requestPermissions()");
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermissions(): Boolean{
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            android.os.Environment.isExternalStorageManager())
        {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                updateCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Denied by user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean{
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    override fun onLocationChanged(location: Location) {
        tvLat.setText(location.latitude.toString())
        tvLon.setText(location.longitude.toString())
        tvAlt.setText(location.altitude.toString())
        tvTime.setText(location.time.toString())
        val file = File(filesDir, "locations.json")
        val exfile = File("/storage/emulated/0/Documents", "locations.json")
        CurrentLoc(location.latitude, location.longitude, location.altitude, location.time).saveInFile(file, this)
        CurrentLoc(location.latitude, location.longitude, location.altitude, location.time).saveInFile(exfile, this)
    }
}