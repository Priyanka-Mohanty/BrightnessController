package com.priyanka.brightnesscontroller

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import org.koin.dsl.module

interface BrightnessController {
    fun getSystemBrightness(): Int
    fun setSystemBrightness(brightness: Int)
    fun hasWriteSettingsPermission(): Boolean
}

class BrightnessControllerImpl(private val context: Context) : BrightnessController {

    private val cResolver: ContentResolver = context.contentResolver

    override fun getSystemBrightness(): Int {
        return try {
            Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            Log.e("BrightnessController", "Cannot access system brightness", e)
            125 // fallback middle value
        }
    }

    override fun setSystemBrightness(brightness: Int) {
        try {
            // Force manual mode so the system doesn't ignore our manual value due to adaptive brightness
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            // Apply minimum bounds check matching legacy logic (0 if <= 20)
            val finalBrightness = if (brightness <= 20) 0 else brightness
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, finalBrightness)
        } catch (e: Exception) {
            Log.e("BrightnessController", "Failed to set system brightness", e)
        }
    }

    override fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }
}

val appModule = module {
    single<BrightnessController> { BrightnessControllerImpl(get()) }
}
