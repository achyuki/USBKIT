package io.github.achyuki.usbkit.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import io.github.achyuki.usbkit.ui.screen.BottomBarDestination
import io.github.achyuki.usbkit.ui.theme.AppTheme
import io.github.achyuki.usbkit.util.ShellUtil

class MainActivity : ComponentActivity() {
    var hasRoot by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    hasRoot = ShellUtil.hasRoot()
                }
                Scaffold(
                    bottomBar = { BottomBar(navController) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    DestinationsNavHost(
                        navController = navController,
                        navGraph = NavGraphs.root,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @Composable
    fun BottomBar(navController: NavHostController) {
        val navigator = navController.rememberDestinationsNavigator()
        NavigationBar {
            BottomBarDestination.entries.filter {
                !it.rootRequired || hasRoot
            }.forEach { destination ->
                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(
                    destination.direction
                )
                NavigationBarItem(
                    selected = isCurrentDestOnBackStack,
                    onClick = {
                        if (isCurrentDestOnBackStack) {
                            navigator.popBackStack(destination.direction, false)
                            return@NavigationBarItem
                        }

                        navigator.navigate(destination.direction) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        if (isCurrentDestOnBackStack) {
                            Icon(
                                destination.iconSelected,
                                contentDescription = stringResource(destination.label)
                            )
                        } else {
                            Icon(
                                destination.iconNotSelected,
                                contentDescription = stringResource(destination.label)
                            )
                        }
                    },
                    label = { Text(stringResource(destination.label)) }
                )
            }
        }
    }
}
