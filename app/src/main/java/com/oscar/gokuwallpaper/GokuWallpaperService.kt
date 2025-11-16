package com.oscar.gokuwallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Color
import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import android.media.MediaPlayer
import android.widget.Toast
import kotlin.math.sin
import kotlin.math.abs

class GokuWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        private var currentBatteryLevel = -1
        private var animationTime = 0f
        private var currentImageRes = R.drawable.fase1
        private var mediaPlayer: MediaPlayer? = null
        private var isCharging = false
        private var chargingAnimationIntensity = 0f
        
        private val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("GokuWallpaper", "Broadcast received: ${intent?.action}")
                
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        Log.d("GokuWallpaper", "POWER CONNECTED!")
                        handler.post {
                            Toast.makeText(applicationContext, "Cargando Ki! ⚡", Toast.LENGTH_SHORT).show()
                        }
                        isCharging = true
                        playChargingSound()
                        startChargingAnimation()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        Log.d("GokuWallpaper", "POWER DISCONNECTED!")
                        handler.post {
                            Toast.makeText(applicationContext, "Ki estable", Toast.LENGTH_SHORT).show()
                        }
                        isCharging = false
                        chargingAnimationIntensity = 0f
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val newLevel = getBatteryLevel()
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCurrentlyCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                                 status == BatteryManager.BATTERY_STATUS_FULL
                        
                        if (isCurrentlyCharging != isCharging) {
                            isCharging = isCurrentlyCharging
                            if (isCharging) {
                                playChargingSound()
                                startChargingAnimation()
                            } else {
                                chargingAnimationIntensity = 0f
                            }
                        }
                        
                        Log.d("GokuWallpaper", "Battery level: $newLevel%, Charging: $isCharging")
                        
                        if (shouldUpdateWallpaper(newLevel)) {
                            currentBatteryLevel = newLevel
                            updateWallpaperBasedOnBattery()
                        }
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            
            Log.d("GokuWallpaper", "WallpaperEngine onCreate")
            
            auraPaint.apply {
                style = Paint.Style.FILL
                maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)
            }
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            
            try {
                registerReceiver(batteryReceiver, filter)
                Log.d("GokuWallpaper", "Receiver registered successfully")
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error registering receiver", e)
            }
            
            val batteryLevel = getBatteryLevel()
            currentBatteryLevel = batteryLevel
            currentImageRes = getImageForBatteryLevel(batteryLevel)
            
            handler.post {
                Toast.makeText(applicationContext, "Goku Wallpaper activo! Batería: $batteryLevel%", Toast.LENGTH_SHORT).show()
            }
            
            startAnimation()
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("GokuWallpaper", "WallpaperEngine onDestroy")
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error unregistering receiver", e)
            }
            running = false
            handler.removeCallbacksAndMessages(null)
            releaseMediaPlayer()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            Log.d("GokuWallpaper", "Visibility changed: $visible")
            running = visible
            if (visible) {
                startAnimation()
            } else {
                handler.removeCallbacksAndMessages(null)
                releaseMediaPlayer()
            }
        }

        private fun startAnimation() {
            if (running) {
                animationTime += 0.05f
                
                if (isCharging && chargingAnimationIntensity < 1f) {
                    chargingAnimationIntensity += 0.02f
                } else if (!isCharging && chargingAnimationIntensity > 0f) {
                    chargingAnimationIntensity -= 0.05f
                }
                
                drawFrame(currentImageRes)
                handler.postDelayed({ startAnimation() }, 33)
            }
        }

        private fun startChargingAnimation() {
            chargingAnimationIntensity = 0f
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

        private fun getAuraColorForPhase(phase: Int): Int {
            return when (phase) {
                1 -> Color.argb(180, 255, 215, 0)    // Amarillo dorado
                2 -> Color.argb(180, 255, 255, 0)    // Amarillo brillante
                3 -> Color.argb(180, 255, 140, 0)    // Naranja dorado
                4 -> Color.argb(180, 65, 105, 225)   // Azul royal
                5 -> Color.argb(180, 200, 200, 255)  // Azul plateado (Ultra Instinto)
                else -> Color.argb(180, 255, 215, 0)
            }
        }

        private fun getSoundForBatteryLevel(level: Int): Int {
            return when {
                level <= 20 -> R.raw.fase1
                level <= 40 -> R.raw.fase2
                level <= 60 -> R.raw.fase3
                level <= 80 -> R.raw.fase4
                else -> R.raw.ultrainstinto
            }
        }

        private fun updateWallpaperBasedOnBattery() {
            val batteryLevel = getBatteryLevel()
            val imageRes = getImageForBatteryLevel(batteryLevel)
            val soundRes = getSoundForBatteryLevel(batteryLevel)
            val phase = getPhaseForBatteryLevel(batteryLevel)
            currentImageRes = imageRes
            
            handler.post {
                Toast.makeText(applicationContext, "Fase $phase activada! ⚡", Toast.LENGTH_SHORT).show()
            }
            
            playPhaseSound(soundRes)
            Log.d("GokuWallpaper", "Updating to Phase $phase (Battery: $batteryLevel%)")
        }

        private fun playPhaseSound(soundRes: Int) {
            try {
                releaseMediaPlayer()
                Log.d("GokuWallpaper", "Attempting to play phase sound: $soundRes")
                mediaPlayer = MediaPlayer.create(applicationContext, soundRes)
                
                if (mediaPlayer != null) {
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                    mediaPlayer?.setOnCompletionListener {
                        Log.d("GokuWallpaper", "Phase sound completed")
                        releaseMediaPlayer()
                    }
                    mediaPlayer?.setOnErrorListener { mp, what, extra ->
                        Log.e("GokuWallpaper", "MediaPlayer error: what=$what, extra=$extra")
                        false
                    }
                    mediaPlayer?.start()
                    Log.d("GokuWallpaper", "Phase sound started")
                } else {
                    Log.e("GokuWallpaper", "MediaPlayer is null")
                }
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error playing phase sound", e)
            }
        }

        private fun playChargingSound() {
            try {
                releaseMediaPlayer()
                Log.d("GokuWallpaper", "Attempting to play charging sound")
                mediaPlayer = MediaPlayer.create(applicationContext, R.raw.charging)
                
                if (mediaPlayer != null) {
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                    mediaPlayer?.setOnCompletionListener {
                        Log.d("GokuWallpaper", "Charging sound completed")
                        releaseMediaPlayer()
                    }
                    mediaPlayer?.setOnErrorListener { mp, what, extra ->
                        Log.e("GokuWallpaper", "MediaPlayer error: what=$what, extra=$extra")
                        false
                    }
                    mediaPlayer?.start()
                    Log.d("GokuWallpaper", "Charging sound started")
                } else {
                    Log.e("GokuWallpaper", "MediaPlayer is null for charging sound")
                }
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error playing charging sound", e)
            }
        }

        private fun releaseMediaPlayer() {
            try {
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                Log.e("GokuWallpaper", "Error releasing media player", e)
            }
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
                    
                    // Goku permanece estático
                    val scaleX = canvasWidth / bitmapWidth
                    val scaleY = canvasHeight / bitmapHeight
                    val scale = maxOf(scaleX, scaleY) * 0.85f
                    
                    val scaledWidth = bitmapWidth * scale
                    val scaledHeight = bitmapHeight * scale
                    
                    val left = (canvasWidth - scaledWidth) / 2f
                    val top = (canvasHeight - scaledHeight) / 2f
                    
                    // Dibujar aura animada detrás de Goku
                    val phase = getPhaseForBatteryLevel(currentBatteryLevel)
                    val auraColor = getAuraColorForPhase(phase)
                    
                    // Aura normal pulsante
                    val normalAuraPulse = abs(sin(animationTime * 2f))
                    val normalAuraAlpha = (50 + normalAuraPulse * 100).toInt()
                    
                    auraPaint.color = Color.argb(normalAuraAlpha, 
                        Color.red(auraColor), 
                        Color.green(auraColor), 
                        Color.blue(auraColor))
                    
                    val auraSize = 1.1f + sin(animationTime * 2f) * 0.05f
                    val auraLeft = left - (scaledWidth * (auraSize - 1f) / 2f)
                    val auraTop = top - (scaledHeight * (auraSize - 1f) / 2f)
                    
                    canvas.drawCircle(
                        canvasWidth / 2f,
                        canvasHeight / 2f,
                        (scaledWidth.coerceAtLeast(scaledHeight)) * auraSize * 0.4f,
                        auraPaint
                    )
                    
                    // Efecto de carga intenso cuando está conectado el cargador
                    if (isCharging && chargingAnimationIntensity > 0) {
                        val chargingPulse = abs(sin(animationTime * 8f))
                        val chargingAlpha = (150 * chargingPulse * chargingAnimationIntensity).toInt()
                        
                        auraPaint.color = Color.argb(chargingAlpha, 255, 255, 200)
                        
                        // Múltiples capas de aura vibrante
                        for (i in 1..3) {
                            val layerSize = 1.1f + (sin(animationTime * (4f + i)) * 0.1f * chargingAnimationIntensity)
                            canvas.drawCircle(
                                canvasWidth / 2f,
                                canvasHeight / 2f,
                                (scaledWidth.coerceAtLeast(scaledHeight)) * layerSize * 0.35f,
                                auraPaint
                            )
                        }
                        
                        // Resplandor de pantalla completa
                        val glowAlpha = (abs(sin(animationTime * 6f)) * 80 * chargingAnimationIntensity).toInt()
                        canvas.drawColor(Color.argb(glowAlpha, 255, 255, 200))
                    }
                    
                    // Dibujar Goku (sin movimiento)
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
