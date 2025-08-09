package io.github.achyuki.usbkit.util

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ShellUtil {
    private val mutex = Mutex()

    suspend fun hasRoot(): Boolean = mutex.withLock {
        lazy {
            try {
                Shell.getShell()
                true
            } catch (e: Exception) {
                false
            }
        }.value
    }
}
