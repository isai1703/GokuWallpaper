package com.oscar.gokuwallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.WallpaperManager
import android.graphics.BitmapFactory

class PowerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fase1)
            wallpaperManager.setBitmap(bitmap)
        }
    }
}
