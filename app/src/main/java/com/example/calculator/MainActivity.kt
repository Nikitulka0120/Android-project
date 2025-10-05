package com.example.calculator

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class CalcButton(
    private val button: Button,
    private val mainText: TextView,
) {
    init {
        if (button.text == "AC"){
            button.setOnClickListener {
                mainText.text = ""
            }
        }
        else if (button.text == "⌫"){
            button.setOnClickListener {
                val str = mainText.text.toString()
                if(str.isNotEmpty()){
                    mainText.text = str.substring(0, str.length - 1)
                }
            }
        }
        else if (button.text == "="){
            button.setOnClickListener {
                mainText.text = calculate(mainText.text.toString())
            }
        }
        else{button.setOnClickListener {
            mainText.append(button.text)
        }}

            }
        }


private fun calculate(expression: String): String {
    return when {
        expression.contains("+") -> {
            val parts = expression.split("+")
            (parts[0].toDouble() + parts[1].toDouble()).toString()
        }
        expression.contains("-") -> {
            val parts = expression.split("-")
            (parts[0].toDouble() - parts[1].toDouble()).toString()
        }
        expression.contains("x") -> {
            val parts = expression.split("x")
            (parts[0].toDouble() * parts[1].toDouble()).toString()
        }
        expression.contains("÷") -> {
            val parts = expression.split("÷")
            val b = parts[1].toDouble()
            if (b == 0.0) "Ошибка" else (parts[0].toDouble() / b).toString()
        }
        else -> expression
    }
}



class MainActivity : AppCompatActivity() {
    private lateinit var mainText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mainText = findViewById(R.id.main_text)

        listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_plus, R.id.btn_minus, R.id.btn_umnozenie,
            R.id.delenie, R.id.skobkaleft, R.id.skobkaright, R.id.btn_zapyataya,
            R.id.AC, R.id.btn_delete,R.id.btn_ravno
        ).forEach { id ->
            CalcButton(findViewById(id), mainText)
        }
    }
}