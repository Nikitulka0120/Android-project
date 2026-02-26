package com.example.calculator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var goToCalcBTN: Button
    private lateinit var goToPlayerBTN: Button
    private lateinit var goToLocationBTN: Button
    private lateinit var goToMobileBTN: Button
    private lateinit var goToZMQBTN: Button
    private lateinit var goToNetworkMapBTN: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        goToCalcBTN = findViewById(R.id.GoToCalc)
        goToCalcBTN.setOnClickListener{
            val goToCalc = Intent(this, CalculatorActivity::class.java)
            startActivity(goToCalc)
        };
        goToPlayerBTN = findViewById(R.id.GoToPlayer)
        goToPlayerBTN.setOnClickListener{
            val goToPlayer = Intent(this, MediaPlayer::class.java)
            startActivity(goToPlayer)
        };
        goToLocationBTN = findViewById(R.id.GoToGeo)
        goToLocationBTN.setOnClickListener {
            val goToLocation = Intent(this, Location::class.java)
            startActivity(goToLocation)
        }
        goToMobileBTN = findViewById(R.id.GoToMobileData)
        goToMobileBTN.setOnClickListener {
            val goToMobile = Intent(this, MobileData::class.java)
            startActivity(goToMobile)
        }
        goToZMQBTN = findViewById(R.id.goToZMQ)
        goToZMQBTN.setOnClickListener {
            val goToZMQ = Intent(this, ZeroMQ::class.java)
            startActivity(goToZMQ)
        }
        goToNetworkMapBTN = findViewById(R.id.mapBTN)
        goToNetworkMapBTN.setOnClickListener {
            val goToNetworkMap = Intent(this, NetworkMap::class.java)
            startActivity(goToNetworkMap)
        }
    }
}