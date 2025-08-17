package io.github.achyuki.usbkit.util

import io.github.achyuki.usbkit.service.RemoteFileSystemService
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

private var kernelConfigCache: Map<String, String>? = null

fun getKernelConfig(remoteFS: RemoteFileSystemService): Map<String, String> {
    // It's not working, idk why
    // val file = remoteFS.getFile("/proc/config.gz")

    val gzbin = remoteFS.readFileBytes("/proc/config.gz")
    return kernelConfigCache ?: BufferedReader(
        InputStreamReader(
            GZIPInputStream(
                ByteArrayInputStream(gzbin)
            )
        )
    ).use { reader ->
        val configMap = mutableMapOf<String, String>()
        reader.lineSequence()
            .filter { line ->
                val trimmed = line.trim()
                !trimmed.startsWith("#") && trimmed.isNotEmpty() && trimmed.contains('=')
            }
            .forEach { line ->
                val trimmed = line.trim()
                val pair = trimmed.indexOf('=')
                val key = trimmed.substring(0, pair).trim()
                val value = trimmed.substring(pair + 1).trim().removeSurrounding("\"")
                if (key.isNotEmpty()) configMap[key] = value
            }
        kernelConfigCache = configMap
        configMap
    }
}
