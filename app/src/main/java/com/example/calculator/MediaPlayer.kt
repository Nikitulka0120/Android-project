package com.example.calculator

import android.Manifest.permission.READ_MEDIA_AUDIO
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.Context
import android.graphics.Color
import android.media.AudioManager

class MediaPlayer : AppCompatActivity() {
    private lateinit var MyPlayList: ListView
    private var mediaPlayer: MediaPlayer? = null
    private val trackNames = mutableListOf<String>()
    private val trackPaths = mutableListOf<String>()

    private lateinit var currentText: TextView

    private lateinit var PlayBTN: ImageButton

    private lateinit var seekBar: SeekBar
    private lateinit var VolumeBar: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var textTotalTime: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_player)

        MyPlayList = findViewById(R.id.tracksListView)
        currentText = findViewById(R.id.currentTrackTitle)
        PlayBTN = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        VolumeBar = findViewById(R.id.volume)

        textCurrentTime = findViewById(R.id.CurrentTime)
        textTotalTime = findViewById(R.id.TotalTime)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showTracksList()
            } else {
                Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_LONG).show()
            }
        }
        requestPermissionLauncher.launch(READ_MEDIA_AUDIO)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        VolumeBar.max = maxVolume
        VolumeBar.progress = currentVolume

        VolumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        PlayBTN.setOnClickListener {
            when {
                mediaPlayer == null -> {
                    Toast.makeText(this, "Сначала выберите трек", Toast.LENGTH_SHORT).show()
                }
                mediaPlayer!!.isPlaying -> {
                    mediaPlayer!!.pause()
                    PlayBTN.setBackgroundColor(Color.parseColor("#795a7d"))
                }
                else -> {
                    mediaPlayer!!.start()
                    updateSeekBar()
                    PlayBTN.setBackgroundColor(Color.parseColor("#53377a"))
                }
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    textCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun showTracksList() {
        val musicFolder = File("/storage/emulated/0/Music")
        getFilesFromFolder(musicFolder)

        if (trackNames.isEmpty()) {
            Toast.makeText(this, "Папка Music пуста", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.item_track,
            android.R.id.text1,
            trackNames
        )
        MyPlayList.adapter = adapter

        MyPlayList.setOnItemClickListener { parent, view, position, id ->
            val trackPath = trackPaths[position]
            playTrack(trackPath)
        }
    }

    private fun getFilesFromFolder(folder: File) {
        trackNames.clear()
        trackPaths.clear()
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    trackNames.add(file.name)
                    trackPaths.add(file.absolutePath)
                }
            }
        }
    }

    private fun playTrack(path: String) {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }

        seekBar.max = mediaPlayer!!.duration
        textTotalTime.text = formatTime(mediaPlayer!!.duration.toLong())
        currentText.text = File(path).name

        updateSeekBar()

        Toast.makeText(this, "Играет: ${File(path).name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateSeekBar() {
        mediaPlayer?.let { player ->
            if (!isUserSeeking) {
                seekBar.progress = player.currentPosition
                textCurrentTime.text = formatTime(player.currentPosition.toLong())
            }

            if (player.isPlaying) {
                handler.postDelayed({ updateSeekBar() }, 500)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
