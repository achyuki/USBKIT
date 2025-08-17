package io.github.achyuki.usbkit

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import io.github.achyuki.usbkit.BuildConfig

const val TAG: String = "USBKIT"
lateinit var appContext: Application
private const val TIMEOUT_S = 10L

class App : Application() {
    init {
        Shell.enableVerboseLogging = false // or BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setInitializers(ShellInitializer::class.java)
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(TIMEOUT_S)
        )
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    private class ShellInitializer : Shell.Initializer() {
        override fun onInit(context: Context, shell: Shell): Boolean = shell.isRoot
    }
}
