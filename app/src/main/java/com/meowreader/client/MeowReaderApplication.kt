package com.meowreader.client

import android.app.Application
import com.google.android.material.color.DynamicColors

class MeowReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply MD3 Dynamic Color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
