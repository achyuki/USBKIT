package io.github.achyuki.usbkit.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StorageScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import io.github.achyuki.usbkit.R

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val rootRequired: Boolean
) {
    Home(HomeScreenDestination, R.string.home, Icons.Filled.Home, Icons.Outlined.Home, false),
    Storage(
        StorageScreenDestination,
        R.string.storage,
        Icons.Filled.Dns,
        Icons.Outlined.Dns,
        true
    ),
    Module(
        ModuleScreenDestination,
        R.string.module,
        Icons.Filled.Extension,
        Icons.Outlined.Extension,
        true
    ),
    Setting(
        SettingScreenDestination,
        R.string.settings,
        Icons.Filled.Settings,
        Icons.Outlined.Settings,
        false
    )
}
