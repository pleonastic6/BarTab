package de.arturo.bartab.ui.navigation

sealed class BarTabDestination(val route: String, val label: String) {
    data object Sales : BarTabDestination("sales", "Verkauf")
    data object History : BarTabDestination("history", "Historie")
    data object Products : BarTabDestination("products", "Produkte")
}
