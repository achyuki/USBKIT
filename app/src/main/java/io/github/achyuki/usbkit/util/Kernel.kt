package io.github.achyuki.usbkit.util

import com.topjohnwu.superuser.Shell

val kernelConfig by lazy {
    // It's not working, idk why
    // val file = remoteFS.getFile("/proc/config.gz")

    val result = Shell.cmd("cat /proc/config.gz | gzip -d").exec()
    if (!result.isSuccess()) throw Exception(result.getErr().joinToString("\n"))
    val configMap = mutableMapOf<String, String>()
    for (line in result.getOut()) {
        val trimmed = line.trim()
        if (!trimmed.startsWith("#") && trimmed.isNotEmpty() &&
            trimmed.contains('=')
        ) {
            val pair = trimmed.indexOf('=')
            val key = trimmed.substring(0, pair).trim()
            val value = trimmed.substring(pair + 1).trim().removeSurrounding("\"")
            if (key.isNotEmpty()) configMap[key] = value
        }
    }
    configMap
}
