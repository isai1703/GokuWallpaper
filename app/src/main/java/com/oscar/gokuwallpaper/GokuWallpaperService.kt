import com.oscar.gokuwallpaper.R
import com.oscar.gokuwallpaper.R
package com.oscar.gokuwallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.*
import android.os.*
import android.view.SurfaceHolder
import android.media.MediaPlayer

class GokuWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = GokuEngine()

    inner class GokuEngine : Engine() {
        private var running = true
        private val paint = Paint()
        private var batteryLevel = 0
        private var charging = false
        private var mp: MediaPlayer? = null

        private val handler = Handler(Looper.getMainLooper())
        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (running) handler.postDelayed(this, 50)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) handler.post(drawRunnable)
            else handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            val canvas = holder.lockCanvas() ?: return

            val bg = if (charging) Color.BLACK else Color.DKGRAY
            canvas.drawColor(bg)

            val text = when {
                batteryLevel <= 20 -> "Fase 1"
                batteryLevel <= 40 -> "Fase 2"
                batteryLevel <= 60 -> "Fase 3"
                batteryLevel <= 80 -> "Fase 4"
                else -> "Ultra Instinto"
            }

            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.CYAN
            paint.textSize = 80f
            canvas.drawText("Oscar (Wokee el Saiyayin Legendario)", canvas.width / 2f, canvas.height / 2f, paint)
            paint.textSize = 50f
            canvas.drawText(text, canvas.width / 2f, canvas.height / 2f + 100, paint)

            holder.unlockCanvasAndPost(canvas)
        }

        fun setChargingState(state: Boolean, level: Int) {
            charging = state
            batteryLevel = level
            playSoundForLevel()
        }

        private fun playSoundForLevel() {
            mp?.release()
            mp = when {
                batteryLevel <= 20 -> MediaPlayer.create(this@GokuWallpaperService, R.raw.phase1)
                batteryLevel <= 40 -> MediaPlayer.create(this@GokuWallpaperService, R.raw.phase2)
                batteryLevel <= 60 -> MediaPlayer.create(this@GokuWallpaperService, R.raw.phase3)
                batteryLevel <= 80 -> MediaPlayer.create(this@GokuWallpaperService, R.raw.phase4)
                else -> MediaPlayer.create(this@GokuWallpaperService, R.raw.ultrainstinct)
            }
            mp?.start()
        }
    }
}
