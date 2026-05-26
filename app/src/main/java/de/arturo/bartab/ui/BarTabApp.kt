package de.arturo.bartab.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.navigation.BarTabDestination
import de.arturo.bartab.ui.screens.analytics.AnalyticsScreen
import de.arturo.bartab.ui.screens.history.HistoryScreen
import de.arturo.bartab.ui.screens.history.SaleDetailScreen
import de.arturo.bartab.ui.screens.products.ProductsScreen
import de.arturo.bartab.ui.screens.sales.SalesScreen

@Composable
fun BarTabApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val state: BarTabViewModel = viewModel(factory = BarTabViewModel.factory(application))
    val destinations = listOf(
        BarTabDestination.Sales,
        BarTabDestination.History,
        BarTabDestination.Analytics,
        BarTabDestination.Products,
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentRoute == BarTabDestination.Sales.route) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.currentSaleIsStaff,
                            onCheckedChange = state::toggleCurrentSaleIsStaff,
                        )
                        Text(
                            "Personal",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
                Box {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text("☰")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        destinations.forEach { destination ->
                            DropdownMenuItem(
                                text = {
                                    val suffix = if (currentRoute == destination.route) " •" else ""
                                    Text(destination.label + suffix)
                                },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(destination.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BarTabDestination.Sales.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BarTabDestination.Sales.route) { SalesScreen(state) }
            composable(BarTabDestination.History.route) {
                HistoryScreen(
                    state = state,
                    onSaleClick = { saleId -> navController.navigate(BarTabDestination.SaleDetail.routeFor(saleId)) },
                )
            }
            composable(BarTabDestination.Analytics.route) { AnalyticsScreen(state) }
            composable(BarTabDestination.Products.route) { ProductsScreen(state) }
            composable(
                route = BarTabDestination.SaleDetail.route,
                arguments = listOf(navArgument("saleId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId").orEmpty()
                SaleDetailScreen(
                    sale = state.saleById(saleId),
                    onBack = { navController.popBackStack() },
                    onLoadIntoCart = { state.loadSaleIntoCart(it) },
                    onCancelSale = { state.cancelSale(it) },
                )
            }
        }
    }
}
