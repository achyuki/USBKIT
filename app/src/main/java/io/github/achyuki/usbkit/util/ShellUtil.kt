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

fun suExec(command: String): String = Shell.cmd(command).exec().getOut().joinToString("\n")
