package io.github.achyuki.usbkit.ugc

import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlin.collections.firstOrNull

val PATH_ROOT = "/config/usb_gadget"
var GADGET = "g1"
val PATH_GADGET get() = "$PATH_ROOT/$GADGET"
val PATH_FUNCTIONS get() = "$PATH_GADGET/functions"
val PATH_CONFIGS get() = "$PATH_GADGET/configs"
val PATH_STRINGS get() = "$PATH_GADGET/strings"

fun getFirstChildEFile(file: ExtendedFile): ExtendedFile? = file.listFiles()?.firstOrNull()
fun getConfigEFile(remoteFSM: FileSystemManager): ExtendedFile? = getFirstChildEFile(remoteFSM.getFile(PATH_CONFIGS))
fun getStringEFile(remoteFSM: FileSystemManager): ExtendedFile? = getFirstChildEFile(remoteFSM.getFile(PATH_STRINGS))

fun readEFileLine(file: ExtendedFile) = file.newInputStream().bufferedReader().use { it.readLine() }
fun readEFileByte(file: ExtendedFile) = file.newInputStream().use { it.read() }
fun writeEFile(file: ExtendedFile, content: String) = file.newOutputStream().bufferedWriter().use { it.write(content) }
