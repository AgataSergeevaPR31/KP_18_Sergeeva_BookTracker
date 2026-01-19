package com.example.libro

import android.app.Application
import android.content.Context
import com.example.libro.Database.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LibroApp : Application() {

    companion object {
        private const val PREFS_NAME = "libro_prefs"
        private const val PREF_FIRST_RUN = "first_run"
    }

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean(PREF_FIRST_RUN, true)

        if (isFirstRun) {
            DatabaseInitializer.initializeDatabase(this)
            prefs.edit().putBoolean(PREF_FIRST_RUN, false).apply()
        }
    }
}