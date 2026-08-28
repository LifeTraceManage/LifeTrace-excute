package com.lifetrace.execute

import android.app.Application
import com.lifetrace.execute.data.sync.SyncScheduler

class LifeTraceExecuteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedulePeriodic(this)
    }
}
