package com.priyanka.brightnesscontroller

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BrightnessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BrightnessApp)
            modules(appModule)
        }
    }
}
