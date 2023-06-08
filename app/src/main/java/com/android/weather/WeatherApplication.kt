package com.android.weather

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: update/delete this depending on the client's decision to use night mode
        // This is for setting the night mode depending on the Android Version, if it's equal or
        // greater than Android Q it would use the system defined theme, else will change to night
        // mode when battery saver mode is on
        val nightMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
