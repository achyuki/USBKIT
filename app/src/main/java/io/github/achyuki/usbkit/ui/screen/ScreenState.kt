package io.github.achyuki.usbkit.ui.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.service.RemoteFileSystemService
import io.github.achyuki.usbkit.service.getRemoteFileSystemService
import io.github.achyuki.usbkit.service.isRemoteFileSystemServiceAlive

var screenState by mutableStateOf<ScreenState<RemoteFileSystemService>>(ScreenState.Loading)

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
            val remoteFS = getRemoteFileSystemService()
            screenState = ScreenState.Success(remoteFS)
        } catch (e: Exception) {
            screenState = ScreenState.Error(e.message ?: "Unknown error")
            e.message?.let { Log.e(TAG, it) }
        }
    }
}
