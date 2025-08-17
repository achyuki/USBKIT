package io.github.achyuki.usbkit.ugc.controller

import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.ugc.*
import io.github.achyuki.usbkit.util.suExec

class Gadget(val remoteFSM: FileSystemManager) {
    val EF_GADGET = remoteFSM.getFile(PATH_GADGET)
    val EF_UDC = EF_GADGET.getChildFile("UDC")
    val EF_CONFIG = getConfigEFile(remoteFSM)
    val EF_FUNCTION = remoteFSM.getFile(PATH_FUNCTIONS)
    val EF_STRING = getStringEFile(remoteFSM)
    val EF_PRODUCT = EF_STRING?.getChildFile("product")
    val EF_MANUFACTURER = EF_STRING?.getChildFile("manufacturer")
    var UDCEnable
        get() = readEFileLine(EF_UDC).isNotEmpty()
        set(value) = writeEFile(EF_UDC, if (value) getUDCName() else "")
    var product
        get() = EF_PRODUCT?.let { readEFileLine(EF_PRODUCT) }
        set(value) {
            if (EF_PRODUCT != null && value != null) writeEFile(EF_PRODUCT, value)
        }
    var manufacturer
        get() = EF_MANUFACTURER?.let { readEFileLine(EF_MANUFACTURER) }
        set(value) {
            if (EF_MANUFACTURER != null && value != null) writeEFile(EF_MANUFACTURER, value)
        }

    fun getUDCName(): String = suExec("getprop vendor.usb.controller")
}
