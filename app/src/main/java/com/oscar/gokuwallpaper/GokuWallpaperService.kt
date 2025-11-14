package com.oscar.gokuwallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.Canvas
import android.graphics.Paint
import android.view.SurfaceHolder
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper

class GokuWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {

        private val paint = Paint()
        private val handler = Handler(Looper.getMainLooper())
        private val images = listOf(
            R.drawable.fase1,
            R.drawable.fase2,
            R.drawable.fase3,
            R.drawable.fase4,
            R.drawable.ultrainstinto
        )

        private var currentIndex = 0
        private var running = true

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) drawFrame()
        }

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            val canvas: Canvas = holder.lockCanvas() ?: return

            val bitmap = BitmapFactory.decodeResource(resources, images[currentIndex])
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            holder.unlockCanvasAndPost(canvas)

            currentIndex = (currentIndex + 1) % images.size

            if (running) {
                handler.postDelayed({ drawFrame() }, 5000)
            }
        }
    }
}
