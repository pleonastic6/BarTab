package de.arturo.bartab.ui.navigation

sealed class BarTabDestination(val route: String, val label: String) {
    data object Sales : BarTabDestination("sales", "Verkauf")
    data object History : BarTabDestination("history", "Historie")
    data object Analytics : BarTabDestination("analytics", "Auswertung")
    data object Products : BarTabDestination("products", "Produkte")

    data object SaleDetail : BarTabDestination("sale-detail/{saleId}", "Verkaufsdetail") {
        fun routeFor(saleId: String): String = "sale-detail/$saleId"
    }

    data object ArchiveDayDetail : BarTabDestination("archive-day/{dayKey}", "Tagesarchiv") {
        fun routeFor(dayKey: String): String = "archive-day/$dayKey"
    }
}
