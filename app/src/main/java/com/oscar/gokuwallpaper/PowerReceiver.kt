import com.oscar.gokuwallpaper.R
import com.oscar.gokuwallpaper.R
package com.oscar.gokuwallpaper

import android.content.*
import android.os.BatteryManager

class PowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, GokuWallpaperService::class.java)
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING

        (context.getSystemService(WALLPAPER_SERVICE) as? GokuWallpaperService)?.let {
            it.GokuEngine().setChargingState(charging, level)
        }
    }
}
