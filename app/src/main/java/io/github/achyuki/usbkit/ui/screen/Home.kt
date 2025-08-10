package io.github.achyuki.usbkit.ui.screen

import android.os.Build
import android.system.Os
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
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.achyuki.usbkit.R
import io.github.achyuki.usbkit.service.RemoteFileSystemService

private var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        try {
            val remoteFS = RemoteFileSystemService.getRemoteFileSystemManager()
            screenState = ScreenState.Success(remoteFS)
        } catch (e: Exception) {
            screenState = ScreenState.Error(e.message ?: "Unknown error")
        }
    }

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
                    // Test data
                    val product = "2312DRAABC"
                    val manufacturer = "Xiaomi"
                    val configNum = 1
                    val functionNum = 14

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
                    InfoCard("Enable")
                }
                is ScreenState.Error -> {
                    StatusCard(
                        color = MaterialTheme.colorScheme.errorContainer,
                        icon = Icons.Outlined.Warning,
                        title = "Init Failed",
                        desc = "${state.message}"
                    )
                    InfoCard("Unknown")
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
private fun StatusCard(
    color: Color,
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit = {}
) {
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
private fun InfoCard(option: String? = null) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            @Composable
            fun InfoCardItem(key: String, value: String) {
                Column {
                    Text(text = key, style = MaterialTheme.typography.bodyLarge)
                    Text(text = value, style = MaterialTheme.typography.bodyMedium)
                }
            }

            InfoCardItem("Kernel Release", Os.uname().release)
            InfoCardItem("System Fingerprint", Build.FINGERPRINT)
            if (option != null) {
                InfoCardItem("Kernel Option", option)
                InfoCardItem("Kernel Option", option)
                InfoCardItem("Kernel Option", option)
                InfoCardItem("Kernel Option", option)
                InfoCardItem("Kernel Option", option)
            }
        }
    }
}

@Composable
fun AboutCard() {
    val uriHandler = LocalUriHandler.current

    ElevatedCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler.openUri(stringResource(R.string.repo_url))
                }
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.bodyLarge
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

private sealed class ScreenState {
    object Loading : ScreenState()
    data class Success(val remoteFS: FileSystemManager) : ScreenState()
    data class Error(val message: String) : ScreenState()
}
