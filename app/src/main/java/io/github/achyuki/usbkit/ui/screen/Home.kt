package io.github.achyuki.usbkit.ui.screen

import android.os.Build
import android.system.Os
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.achyuki.usbkit.R
import io.github.achyuki.usbkit.TAG
import io.github.achyuki.usbkit.ugc.*
import io.github.achyuki.usbkit.ugc.controller.Gadget
import io.github.achyuki.usbkit.util.getKernelConfig

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = screenState) {
                is ScreenState.Loading -> {
                    StatusCard(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        icon = Icons.Outlined.HourglassEmpty,
                        title = "Loading",
                        desc = "Function: N/N"
                    )
                    InfoCard()
                }
                is ScreenState.Success -> {
                    val remoteFS = state.pack
                    val remoteFSM = remoteFS.manager
                    val gadget = Gadget(remoteFSM)
                    val product = gadget.product ?: "Unknow"
                    val manufacturer = gadget.manufacturer ?: "Unknow"
                    val configNum = 1
                    val functionNum = 14
                    val tt = remoteFS.readFileBytes("/config/usb_gadget/g1/strings/0x409/product").size
                    Log.e(TAG, "test $tt")

                    StatusCard(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        icon = Icons.Outlined.CheckCircle,
                        title = "$product <$manufacturer>",
                        desc = "Function: $configNum/$functionNum"
                    ) {
                        navigator.navigate(SettingScreenDestination) {
                            popUpTo(HomeScreenDestination) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    var kernelConfig: Map<String, String>? = null
                    try {
                        kernelConfig = getKernelConfig(remoteFS)
                    } catch (e: Exception) {
                        kernelConfig = emptyMap()
                        e.message?.let { Log.e(TAG, it) }
                    }
                    InfoCard(kernelConfig)
                }
                is ScreenState.Error -> {
                    StatusCard(
                        color = MaterialTheme.colorScheme.errorContainer,
                        icon = Icons.Outlined.Warning,
                        title = "Init Failed",
                        desc = "${state.message}"
                    )
                    InfoCard(emptyMap())
                }
            }
            AboutCard()
            Spacer(Modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigator: DestinationsNavigator, scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                navigator.navigate(SettingScreenDestination) {
                    popUpTo(HomeScreenDestination) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
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

@Composable
private fun StatusCard(color: Color, icon: ImageVector, title: String, desc: String, onClick: () -> Unit = {}) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Column(Modifier.padding(start = 20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun InfoCard(kernelConfig: Map<String, String>? = null) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            @Composable
            fun InfoCardItem(key: String, value: String) {
                Column {
                    Text(text = key, style = MaterialTheme.typography.titleMedium)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium)
                }
            }

            InfoCardItem("Kernel Release", Os.uname().release)
            InfoCardItem("System Fingerprint", Build.FINGERPRINT)
            if (kernelConfig != null) {
                val optionList = listOf(
                    "Libcomposite" to "CONFIG_USB_LIBCOMPOSITE",
                    "Configfs" to "CONFIG_USB_CONFIGFS",
                    "Configfs Functionfs" to "CONFIG_USB_CONFIGFS_F_FS",
                    "Configfs Mass Storage" to "CONFIG_USB_CONFIGFS_MASS_STORAGE",
                    "Configfs HID" to "CONFIG_USB_CONFIGFS_F_HID",
                    "Configfs RNDIS" to "CONFIG_USB_CONFIGFS_RNDIS",
                    "Configfs UVC" to "CONFIG_USB_CONFIGFS_F_UVC",
                    "Configfs UAC2" to "CONFIG_USB_CONFIGFS_F_UAC2",
                    "OTG" to "CONFIG_USB_OTG"
                )
                for (option in optionList) {
                    val (name, optionName) = option
                    val state = when {
                        kernelConfig[optionName] == "y" -> "Support"
                        kernelConfig.size > 0 -> "Not Support"
                        else -> "Unknow"
                    }
                    InfoCardItem(name, state)
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.repo_url)

    ElevatedCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler.openUri(url)
                }
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_about_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
