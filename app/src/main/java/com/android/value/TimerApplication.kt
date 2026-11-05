package com.android.value

import android.app.Application
import com.android.value.core.TimerEngine

class TimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimerEngine.initialize(this)
    }
}

