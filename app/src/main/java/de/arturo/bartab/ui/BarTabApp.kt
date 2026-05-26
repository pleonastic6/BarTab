package de.arturo.bartab.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.arturo.bartab.ui.navigation.BarTabDestination
import de.arturo.bartab.ui.screens.history.HistoryScreen
import de.arturo.bartab.ui.screens.products.ProductsScreen
import de.arturo.bartab.ui.screens.sales.SalesScreen

@Composable
fun BarTabApp() {
    val navController = rememberNavController()
    val destinations = listOf(
        BarTabDestination.Sales,
        BarTabDestination.History,
        BarTabDestination.Products,
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { Text(destination.label.take(1)) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BarTabDestination.Sales.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BarTabDestination.Sales.route) { SalesScreen() }
            composable(BarTabDestination.History.route) { HistoryScreen() }
            composable(BarTabDestination.Products.route) { ProductsScreen() }
        }
    }
}
