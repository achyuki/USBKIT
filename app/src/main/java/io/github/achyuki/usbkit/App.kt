package io.github.achyuki.usbkit

import android.app.Application

lateinit var appContext: Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
