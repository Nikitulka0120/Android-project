package com.example.calculator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class ZeroMQ : AppCompatActivity() {
    private var log_tag: String = "MY_LOG_TAG"
    private lateinit var LogConnection: TextView
    private lateinit var StartSession: Button
    private lateinit var handler: Handler
    private var zmqContext: ZContext? = null
    private var serverThread: Thread? = null

    private lateinit var ConnectToRemote: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_zero_mq)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        StartSession = findViewById(R.id.startTrade)
        LogConnection = findViewById(R.id.LogerTV)
        ConnectToRemote = findViewById(R.id.startTradeWS)
        LogConnection.movementMethod = ScrollingMovementMethod()
        handler = Handler(Looper.getMainLooper())
        serverThread = Thread { startServer() }
        serverThread?.start()

        StartSession.setOnClickListener {
            StartSession.isEnabled = false
            LogConnection.append("\n--- Starting New Session ---\n")
            LogConnection.append("\ntcp://127.0.0.1:5555\n")
            val clientThread = Thread { startClient("tcp://127.0.0.1:5555") }
            clientThread.start()
        }
        ConnectToRemote.setOnClickListener {
            ConnectToRemote.isEnabled = false
            LogConnection.append("\n--- Starting New Session ---\n")
            LogConnection.append("\ntcp://155.212.222.104:5555\n")
            val clientThread = Thread { startClient("tcp://155.212.222.104:5555") }
            clientThread.start()
        }
    }

    private fun logToUi(message: String) {
        handler.post {
            LogConnection.append("$message\n")
            val scrollAmount = LogConnection.layout.getLineTop(LogConnection.lineCount) - LogConnection.height
            if (scrollAmount > 0)
                LogConnection.scrollTo(0, scrollAmount)
        }
    }

    fun startServer() {
        if (zmqContext == null) zmqContext = ZContext()
        val socket = zmqContext!!.createSocket(SocketType.REP)
        socket.linger = 0

        try {
            socket.bind("tcp://127.0.0.1:5555")
            logToUi("SERVER: Local server ready on port 5555...")

            while (!Thread.currentThread().isInterrupted) {
                val requestBytes = socket.recv(0) ?: break

                val request = String(requestBytes, ZMQ.CHARSET)
                logToUi("SERVER: Received -> '$request'")
                Thread.sleep(200)
                val response = "Hello from Server!"
                socket.send(response.toByteArray(ZMQ.CHARSET), 0)
                logToUi("SERVER: Sent reply")
            }
        } catch (e: Exception) {
            Log.e(log_tag, "Server error", e)
        } finally {
            socket.close()
            logToUi("SERVER: Stopped")
        }
    }

    fun startClient(ipAddress: String) {
        try {
            ZContext().use { context ->
                val socket = context.createSocket(SocketType.REQ)
                socket.linger = 0
                socket.connect(ipAddress)
                for (i in 1..10) {
                    val request = "Hello from Client #$i"
                    socket.send(request.toByteArray(ZMQ.CHARSET), 0)
                    val replyBytes = socket.recv(0)
                    if (replyBytes != null) {
                        val reply = String(replyBytes, ZMQ.CHARSET)
                        logToUi("CLIENT: Received -> '$reply'")
                    }
                    Thread.sleep(300)
                }
                logToUi("--- Session Finished ---")
            }
        } catch (e: Exception) {
            logToUi("CLIENT ERROR: ${e.message}")
        } finally {
            handler.post { StartSession.isEnabled = true }
            handler.post { ConnectToRemote.isEnabled = true }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        zmqContext?.close()
        zmqContext = null
        serverThread?.interrupt()
    }
}