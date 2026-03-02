package com.maintx.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maintx.feature.diagnostics.DiagnosticsScreen
import com.maintx.feature.importexport.ImportExportScreen
import com.maintx.feature.ocr.OcrScreen
import com.maintx.feature.partsbin.PartsBinScreen
import com.maintx.feature.servicelog.ServiceLogScreen
import com.maintx.feature.vehicle.VehicleScreen

@Composable
fun MaintXNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val destinations = MaintXDestination.entries
    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(destination.title) },
                        icon = {}
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MaintXDestination.Vehicle.route,
            modifier = Modifier
        ) {
            composable(MaintXDestination.Vehicle.route) { VehicleScreen(innerPadding) }
            composable(MaintXDestination.ServiceLog.route) { ServiceLogScreen(innerPadding) }
            composable(MaintXDestination.PartsBin.route) { PartsBinScreen(innerPadding) }
            composable(MaintXDestination.Ocr.route) { OcrScreen(innerPadding) }
            composable(MaintXDestination.Diagnostics.route) { DiagnosticsScreen(innerPadding) }
            composable(MaintXDestination.ImportExport.route) { ImportExportScreen(innerPadding) }
        }
    }
}
