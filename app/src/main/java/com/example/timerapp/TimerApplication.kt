package com.example.timerapp

import android.app.Application
import com.example.timerapp.repository.TimerRepository
import timber.log.Timber

class TimerApplication : Application() {

    val timerRepository: TimerRepository
        get() = ServiceLocator.provideTimerRepository(this)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}