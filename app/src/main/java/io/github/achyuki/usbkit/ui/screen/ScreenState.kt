package io.github.achyuki.usbkit.ui.screen

sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Success<out T>(val pack: T) : ScreenState<T>()
    data class Error(val message: String) : ScreenState<Nothing>()
}
