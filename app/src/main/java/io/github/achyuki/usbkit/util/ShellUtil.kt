package io.github.achyuki.usbkit.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.topjohnwu.superuser.Shell
import io.github.achyuki.usbkit.BuildConfig

object ShellUtil {
    var isInitialized by mutableStateOf(false)
        private set
    val TIMEOUT_MS: Long = 10000
    val TIMEOUT_S: Long = TIMEOUT_MS / 1000

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setInitializers(ShellInitializer::class.java)
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(TIMEOUT_S)
        )
    }

    @Synchronized
    fun getShell(): Shell = Shell.getShell().also {
        isInitialized = true
    }

    private class ShellInitializer : Shell.Initializer() {
        override fun onInit(context: Context, shell: Shell): Boolean = shell.isRoot
    }
}
