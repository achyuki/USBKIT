package io.github.achyuki.usbkit.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.achyuki.usbkit.R
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.ui.component.PreferenceListener
import io.github.achyuki.usbkit.ui.component.createCustomPreferenceFlow
import io.github.achyuki.usbkit.util.Ref
import kotlinx.coroutines.flow.*
import me.zhanghai.compose.preference.*

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SettingScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    loadScreen()

    Scaffold(
        topBar = {
            TopBar(navigator, scrollBehavior)
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        )
    ) { innerPadding ->
        val listener = remember {
            @Suppress("UNCHECKED_CAST")
            object : PreferenceListener {
                override fun <T> onRead(key: String, ref: Ref<T>) {
                    if (key == "test1") ref.value = true as T
                    Log.e(TAG, "read $key")
                }
                override fun <T> onWrite(key: String, ref: Ref<T>): Boolean {
                    if (key == "test2") return false
                    Log.e(TAG, "write $key")
                    return true // Allow write
                }
            }
        }
        val preferenceFlow = createCustomPreferenceFlow(listener)
        ProvidePreferenceLocals(
            flow = preferenceFlow
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                switchPreference(
                    key = "test1",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = {
                        Text(text = if (screenState is ScreenState.Loading) "On" else "Off")
                    }
                )
                switchPreference(
                    key = "test2",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test3",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = "Test3") }
                )
                switchPreference(
                    key = "test4",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test5",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test6",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test7",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test8",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test9",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
                switchPreference(
                    key = "test10",
                    defaultValue = false,
                    title = { Text(text = "Switch preference") },
                    icon = {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                    },
                    summary = { Text(text = if (it) "On" else "Off") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigator: DestinationsNavigator, scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        actions = {
            IconButton(onClick = {
                // ...
            }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        scrollBehavior = scrollBehavior
    )
}
