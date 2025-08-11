package io.github.achyuki.usbkit.ui.component

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import io.github.achyuki.usbkit.util.Ref
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import me.zhanghai.compose.preference.MapPreferences
import me.zhanghai.compose.preference.Preferences

interface PreferenceListener {
    fun onRead(key: String, ref: Ref<Any>)
    fun onWrite(key: String, ref: Ref<Any>): Boolean
}

@Composable
fun createCustomPreferenceFlow(
    listener: PreferenceListener? = null
): MutableStateFlow<Preferences> {
    val context = LocalContext.current

    @Suppress("DEPRECATION")
    val sharedPreferences =
        remember {
            android.preference.PreferenceManager.getDefaultSharedPreferences(
                context
            )
        }
    var preferences by remember {
        mutableStateOf<Preferences>(sharedPreferences.preferences)
    }

    fun applyRead(original: Preferences): Preferences {
        if (listener == null) return original
        return MapPreferences(
            original.asMap().mapValues { (key, value) ->
                val valueRef = Ref(value)
                listener.onRead(key, valueRef)
                valueRef.value
            }
        )
    }

    fun applyWrite(original: Preferences): Preferences {
        val oldMap = preferences.asMap()
        return MapPreferences(
            original.asMap().mapNotNull { (key, value) ->
                if (value == oldMap[key]) return@mapNotNull null
                if (listener == null)return@mapNotNull key to value
                val valueRef = Ref(value)
                if (listener.onWrite(key, valueRef))return@mapNotNull key to valueRef.value
                null
            }.toMap()
        )
    }

    return MutableStateFlow(applyRead(preferences)).also {
        LaunchedEffect(it) {
            withContext(Dispatchers.Main.immediate) {
                it.drop(1).collect {
                    val update = applyWrite(it)
                    preferences =
                        MapPreferences(preferences.asMap() + update.asMap()) as Preferences
                    sharedPreferences.preferences = update
                }
            }
        }
    }
}

private var SharedPreferences.preferences: Preferences
    get() =
        @Suppress("UNCHECKED_CAST")
        MapPreferences(all as Map<String, Any>)
    set(value) {
        edit().apply {
            for ((key, mapValue) in value.asMap()) {
                when (mapValue) {
                    is Boolean -> putBoolean(key, mapValue)
                    is Int -> putInt(key, mapValue)
                    is Float -> putFloat(key, mapValue)
                    is String -> putString(key, mapValue)
                    is Set<*> ->
                        @Suppress("UNCHECKED_CAST")
                        putStringSet(key, mapValue as Set<String>)
                    else -> throw IllegalArgumentException("Unsupported type for value $mapValue")
                }
            }
            apply()
        }
    }
