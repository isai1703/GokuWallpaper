package com.oscar.gokuwallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.WallpaperManager

class PowerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val drawable = context.getDrawable(R.drawable.fase1)
            wallpaperManager.setDrawable(drawable)
        }
    }
}
