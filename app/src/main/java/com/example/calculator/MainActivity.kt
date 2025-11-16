package com.example.calculator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.content.Context
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ZaglushkaButton(
    private val button: Button
) {
    init {
        button.setOnClickListener {
            val context = button.context
            val message = "Функционал ${button.text} временно недоступен :("
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(context, message, duration).show()
}

            }
        }



class MainActivity : AppCompatActivity() {
    private lateinit var GoToCalcBTN: Button
    private lateinit var GoToPlayerBTN: Button
    private lateinit var GoToLocationBTN: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        GoToCalcBTN = findViewById(R.id.GoToCalc)
        GoToCalcBTN.setOnClickListener{
            val randomIntent = Intent(this, CalculatorActivity::class.java)
            startActivity(randomIntent)
        };
        GoToPlayerBTN = findViewById(R.id.GoToPlayer)
        GoToPlayerBTN.setOnClickListener{
            val randomIntent = Intent(this, MediaPlayer::class.java)
            startActivity(randomIntent)
        };
        GoToLocationBTN = findViewById(R.id.GoToGeo)
        GoToLocationBTN.setOnClickListener {
            val randomIntent = Intent(this, Location::class.java)
            startActivity(randomIntent)
        }
        listOf(
            R.id.GoToMobileData
        ).forEach { id ->
            ZaglushkaButton(findViewById(id))
        }
    }
}