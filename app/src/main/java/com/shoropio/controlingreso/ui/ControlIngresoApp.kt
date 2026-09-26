package com.shoropio.controlingreso.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shoropio.controlingreso.R
import com.shoropio.controlingreso.domain.model.RecordType
import com.shoropio.controlingreso.ui.dashboard.DashboardScreen
import com.shoropio.controlingreso.ui.detail.DetailScreen
import com.shoropio.controlingreso.ui.entry.EntryScreen
import com.shoropio.controlingreso.ui.history.HistoryScreen
import com.shoropio.controlingreso.ui.navigation.Routes
import com.shoropio.controlingreso.ui.report.ReportScreen
import com.shoropio.controlingreso.ui.settings.SettingsScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ControlIngresoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = Routes.baseOf(backStackEntry?.destination?.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in Routes.topLevel) {
                AppBottomBar(navController, currentRoute)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onNavigate = { navController.navigate(it) })
            }
            composable(Routes.REPORT) {
                ReportScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                Routes.ENTRY,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { navBackEntry ->
                val recordType = RecordType.fromName(navBackEntry.arguments?.getString("type") ?: "")
                    ?: RecordType.VISIT
                EntryScreen(
                    type = recordType,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                DetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem(Routes.DASHBOARD, "Inicio", Icons.Filled.Home),
        BottomNavItem(Routes.HISTORY, "Historial", Icons.Filled.History),
        BottomNavItem(Routes.REPORT, "Reportes", Icons.Filled.BarChart),
        BottomNavItem(Routes.SETTINGS, "Configuración", Icons.Filled.Settings),
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}