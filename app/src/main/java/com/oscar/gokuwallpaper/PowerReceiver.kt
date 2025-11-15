package com.oscar.gokuwallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.util.Log

class PowerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("GokuWallpaper", "PowerReceiver triggered: ${intent?.action}")
        
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                updateWallpaper(context)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                updateWallpaper(context)
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                updateWallpaper(context)
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val batteryStatus = context.registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level.toFloat() / scale.toFloat() * 100).toInt()
        } else {
            0
        }
    }

    private fun getImageForBatteryLevel(level: Int): Int {
        return when {
            level <= 20 -> R.drawable.fase1
            level <= 40 -> R.drawable.fase2
            level <= 60 -> R.drawable.fase3
            level <= 80 -> R.drawable.fase4
            else -> R.drawable.ultrainstinto
        }
    }

    private fun updateWallpaper(context: Context) {
        try {
            val batteryLevel = getBatteryLevel(context)
            val imageRes = getImageForBatteryLevel(batteryLevel)
            
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeResource(context.resources, imageRes)
            wallpaperManager.setBitmap(bitmap)
            
            Log.d("GokuWallpaper", "Wallpaper updated for battery level: $batteryLevel%")
        } catch (e: Exception) {
            Log.e("GokuWallpaper", "Error updating wallpaper", e)
        }
    }
}
