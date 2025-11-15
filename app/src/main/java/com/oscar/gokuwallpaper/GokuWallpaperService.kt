package com.oscar.gokuwallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.BatteryManager

class GokuWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        
        private val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateWallpaperBasedOnBattery()
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
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(batteryReceiver)
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
            drawFrame(imageRes)
        }

        private fun drawFrame(imageRes: Int) {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val originalBitmap = BitmapFactory.decodeResource(resources, imageRes)
                    val scaledBitmap = scaleBitmapToScreen(originalBitmap, canvas.width, canvas.height)
                    
                    canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
                    
                    if (scaledBitmap != originalBitmap) {
                        scaledBitmap.recycle()
                    }
                    originalBitmap.recycle()
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun scaleBitmapToScreen(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            
            val scaleWidth = screenWidth.toFloat() / bitmapWidth
            val scaleHeight = screenHeight.toFloat() / bitmapHeight
            val scale = maxOf(scaleWidth, scaleHeight)
            
            val scaledWidth = (bitmapWidth * scale).toInt()
            val scaledHeight = (bitmapHeight * scale).toInt()
            
            return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        }
    }
}
