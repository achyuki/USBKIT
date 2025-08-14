package io.github.achyuki.usbkit.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.service.getRemoteFileSystemManager
import io.github.achyuki.usbkit.service.isRemoteFileSystemServiceAlive

var screenState by mutableStateOf<ScreenState<FileSystemManager>>(ScreenState.Loading)

sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Success<out T>(val pack: T) : ScreenState<T>()
    data class Error(val message: String) : ScreenState<Nothing>()
}

@Composable
fun loadScreen() {
    if (screenState is ScreenState.Success && isRemoteFileSystemServiceAlive) return
    LaunchedEffect(Unit) {
        screenState = ScreenState.Loading
        try {
            val remoteFS = getRemoteFileSystemManager()
            screenState = ScreenState.Success(remoteFS)
        } catch (e: Exception) {
            screenState = ScreenState.Error(e.message ?: "Unknown error")
        }
    }
}
