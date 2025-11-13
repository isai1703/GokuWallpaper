package com.example.gokuwallpaper

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.example.gokuwallpaper.R

class MainActivity : AppCompatActivity() {

    private lateinit var wallpaperManager: WallpaperManager
    private lateinit var imageView: ImageView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.gokuImage)
        wallpaperManager = WallpaperManager.getInstance(this)

        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryStatus)
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            updateWallpaper(level)
        }
    }

    private fun updateWallpaper(level: Int) {
        val drawableId: Int
        val soundId: Int

        when (level) {
            in 0..20 -> {
                drawableId = R.drawable.fase1
                soundId = R.raw.phase1
            }
            in 21..40 -> {
                drawableId = R.drawable.fase2
                soundId = R.raw.phase2
            }
            in 41..60 -> {
                drawableId = R.drawable.fase3
                soundId = R.raw.phase3
            }
            in 61..80 -> {
                drawableId = R.drawable.fase4
                soundId = R.raw.phase4
            }
            else -> {
                drawableId = R.drawable.ultrainstinto
                soundId = R.raw.ultrainstinct
            }
        }

        val bitmap = BitmapFactory.decodeResource(resources, drawableId)
        imageView.setImageBitmap(bitmap)
        wallpaperManager.setBitmap(bitmap)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundId)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        unregisterReceiver(batteryReceiver)
    }
}
