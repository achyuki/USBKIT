package io.github.achyuki.usbkit.util

import com.topjohnwu.superuser.Shell

val hasRoot by lazy {
    try {
        Shell.getShell()
        true
    } catch (e: Exception) {
        false
    }
}
