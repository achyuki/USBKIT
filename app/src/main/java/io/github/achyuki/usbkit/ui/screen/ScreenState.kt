package io.github.achyuki.usbkit.ui.screen

import com.topjohnwu.superuser.nio.FileSystemManager

sealed class ScreenState {
    object Loading : ScreenState()
    data class Success(val remoteFS: FileSystemManager) : ScreenState()
    data class Error(val message: String) : ScreenState()
}
