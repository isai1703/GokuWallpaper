package com.oscar.gokuwallpaper

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("GokuWallpaper", "MainActivity onCreate started")
            setContentView(R.layout.activity_main)
            Toast.makeText(this, "Goku Wallpaper Loaded", Toast.LENGTH_SHORT).show()
            Log.d("GokuWallpaper", "MainActivity onCreate finished")
        } catch (e: Exception) {
            Log.e("GokuWallpaper", "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
