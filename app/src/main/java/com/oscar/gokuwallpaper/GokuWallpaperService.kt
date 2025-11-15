package com.oscar.gokuwallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.BatteryManager
import android.util.Log

class GokuWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        private var currentBatteryLevel = -1
        
        private val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val newLevel = getBatteryLevel()
                Log.d("GokuWallpaper", "Battery level: $newLevel%")
                
                if (shouldUpdateWallpaper(newLevel)) {
                    currentBatteryLevel = newLevel
                    updateWallpaperBasedOnBattery()
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(batteryReceiver, filter)
            Log.d("GokuWallpaper", "WallpaperEngine created and receiver registered")
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error unregistering receiver", e)
            }
            running = false
            handler.removeCallbacksAndMessages(null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) {
                updateWallpaperBasedOnBattery()
            }
        }

        private fun getBatteryLevel(): Int {
            val batteryStatus = applicationContext.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            return if (level >= 0 && scale > 0) {
                (level.toFloat() / scale.toFloat() * 100).toInt()
            } else {
                0
            }
        }

        private fun shouldUpdateWallpaper(newLevel: Int): Boolean {
            val oldPhase = getPhaseForBatteryLevel(currentBatteryLevel)
            val newPhase = getPhaseForBatteryLevel(newLevel)
            return oldPhase != newPhase
        }

        private fun getPhaseForBatteryLevel(level: Int): Int {
            return when {
                level <= 20 -> 1
                level <= 40 -> 2
                level <= 60 -> 3
                level <= 80 -> 4
                else -> 5
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

        private fun updateWallpaperBasedOnBattery() {
            val batteryLevel = getBatteryLevel()
            val imageRes = getImageForBatteryLevel(batteryLevel)
            val phase = getPhaseForBatteryLevel(batteryLevel)
            Log.d("GokuWallpaper", "Updating to Phase $phase (Battery: $batteryLevel%)")
            drawFrame(imageRes)
        }

        private fun drawFrame(imageRes: Int) {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val originalBitmap = BitmapFactory.decodeResource(resources, imageRes)
                    
                    val canvasWidth = canvas.width.toFloat()
                    val canvasHeight = canvas.height.toFloat()
                    val bitmapWidth = originalBitmap.width.toFloat()
                    val bitmapHeight = originalBitmap.height.toFloat()
                    
                    val scaleX = canvasWidth / bitmapWidth
                    val scaleY = canvasHeight / bitmapHeight
                    val scale = maxOf(scaleX, scaleY) * 0.85f
                    
                    val scaledWidth = bitmapWidth * scale
                    val scaledHeight = bitmapHeight * scale
                    
                    val left = (canvasWidth - scaledWidth) / 2f
                    val top = (canvasHeight - scaledHeight) / 2f
                    
                    val matrix = Matrix().apply {
                        postScale(scale, scale)
                        postTranslate(left, top)
                    }
                    
                    canvas.drawBitmap(originalBitmap, matrix, paint)
                    originalBitmap.recycle()
                }
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error drawing frame", e)
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}
