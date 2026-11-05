package com.devvardhan.value

import android.app.Application
import com.devvardhan.value.core.TimerEngine

class TimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimerEngine.initialize(this)
    }
}

