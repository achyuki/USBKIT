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
fun createCustomPreferenceFlow(listener: PreferenceListener): MutableStateFlow<Preferences> {
    val context = LocalContext.current
    lateinit var preferenceFlow: MutableStateFlow<Preferences>

    @Suppress("DEPRECATION")
    val sharedPreferences =
        remember {
            android.preference.PreferenceManager.getDefaultSharedPreferences(
                context
            )
        }
    var preferences = remember {
        MutablePipePreferences(
            sharedPreferences.preferences.toMutablePreferences(),
            listener
        ) as Preferences
    }

    preferenceFlow = MutableStateFlow(preferences).also {
        LaunchedEffect(it) {
            withContext(Dispatchers.Main.immediate) {
                it.drop(1).collect {
                    val oldMap = preferences.asMap()
                    val diffMap =
                        it.asMap().filter { (key, value) ->
                            value != oldMap[key]
                        }
                    if (diffMap.size > 0) {
                        val update = MapPreferences(it.asMap())
                        preferenceFlow.value =
                            MutablePipePreferences(
                                update.toMutablePreferences(),
                                listener
                            )
                        preferences = update
                        sharedPreferences.preferences = MapPreferences(diffMap)
                    }
                }
            }
        }
    }
    return preferenceFlow
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

private class MutablePipePreferences(
    private val mutablePreferences: MutablePreferences,
    private val listener: PreferenceListener
) : MutablePreferences {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        var valueCache = mutablePreferences[key] as T?
        val valueRef = Ref(valueCache)
        listener.onRead(key, valueRef)
        return valueRef.value
    }

    override fun asMap(): Map<String, Any> = mutablePreferences.asMap()

    override fun toMutablePreferences(): MutablePreferences =
        MutablePipePreferences(mutablePreferences, listener) // It's important

    override fun <T> set(key: String, value: T?) {
        var valueCache = value
        val valueRef = Ref(valueCache)
        if (listener.onWrite(key, valueRef)) mutablePreferences[key] = valueRef.value
    }

    override fun clear() = mutablePreferences.clear()
}
