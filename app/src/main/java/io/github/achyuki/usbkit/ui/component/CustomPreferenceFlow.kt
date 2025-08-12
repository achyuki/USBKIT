package io.github.achyuki.usbkit.ui.component

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.util.Ref
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import me.zhanghai.compose.preference.MapPreferences
import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences

interface PreferenceListener {
    fun <T> onRead(key: String, ref: Ref<T>)
    fun <T> onWrite(key: String, ref: Ref<T>): Boolean
}

@Composable
fun createCustomPreferenceFlow(
    listener: PreferenceListener? = null
): MutableStateFlow<Preferences> {
    val context = LocalContext.current
    lateinit var test: MutableStateFlow<Preferences>

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

    var preferencesWarp = remember(preferences) {
        if (listener != null) {
            PipePreferences(preferences, listener)
        } else {
            preferences
        }
    }

    fun writeFilter(original: Preferences): Preferences {
        val map =
            original.asMap().filter { (key, value) ->
                value != preferences[key]
            }
        return MapPreferences(map)
    }

    return MutableStateFlow(preferencesWarp).also {
        LaunchedEffect(it) {
            withContext(Dispatchers.Main.immediate) {
                it.drop(1).collect {
                    val filter = writeFilter(it)
                    preferences = MapPreferences(it.asMap())
                    sharedPreferences.preferences = filter
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
                Log.d(TAG, "SP write $key: $mapValue")
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

private class PipePreferences(
    private val preferences: Preferences,
    private val listener: PreferenceListener
) : Preferences {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        var valueCache = preferences[key] as T?
        val valueRef = Ref(valueCache)
        listener.onRead(key, valueRef)
        return valueRef.value
    }

    override fun asMap(): Map<String, Any> = preferences.asMap()

    override fun toMutablePreferences(): MutablePreferences =
        MutablePipePreferences(preferences.toMutablePreferences(), listener)
}

private class MutablePipePreferences(
    private val mutablePreferences: MutablePreferences,
    private val listener: PreferenceListener
) : MutablePreferences {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = mutablePreferences[key] as T?

    override fun asMap(): Map<String, Any> = mutablePreferences.asMap()

    override fun toMutablePreferences(): MutablePreferences = this

    override fun <T> set(key: String, value: T?) {
        var valueCache = value
        val valueRef = Ref(valueCache)
        if (listener.onWrite(key, valueRef)) mutablePreferences[key] = valueRef.value
    }

    override fun clear() = mutablePreferences.clear()
}
