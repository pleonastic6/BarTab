package de.arturo.bartab.ui.screens.analytics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.ArchivedDay
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.state.ProductSalesSummary
import de.arturo.bartab.ui.components.toEuroString
import de.arturo.bartab.ui.export.shareCsv
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    state: BarTabViewModel,
    onOpenArchivedDay: (String) -> Unit,
) {
    val summary = state.todaySummary
    val soldProducts = state.todayProductSummaries
    val staffDrinks = state.todayStaffDrinkSummaries
    val archivedDays = state.archivedDays
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Auswertung", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        shareCsv(
                            context = context,
                            fileName = state.exportFileName(),
                            content = state.buildTodayCsv(),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("CSV exportieren")
                }
                OutlinedButton(
                    onClick = state::archiveToday,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isTodayArchived) "Tagesabschluss aktualisieren" else "Tagesabschluss")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Heute", fontWeight = FontWeight.Bold)
                    SummaryRow("Umsatz", summary.revenueCents.toEuroString())
                    SummaryRow("Verkäufe", summary.completedSalesCount.toString())
                    SummaryRow("Personalgetränke", summary.staffSalesCount.toString())
                    SummaryRow("Stornos", summary.cancelledSalesCount.toString())
                }
            }
        }
        item { Text("Verkaufte Getränke", fontWeight = FontWeight.Bold) }
        if (soldProducts.isEmpty()) {
            item { Text("Heute wurden noch keine bezahlten Verkäufe abgeschlossen") }
        } else {
            items(soldProducts, key = { "sold-" + it.productName }) { item -> ProductSummaryCard(item) }
        }
        item { Text("Personalgetränke", fontWeight = FontWeight.Bold) }
        if (staffDrinks.isEmpty()) {
            item { Text("Heute wurden noch keine Personalgetränke dokumentiert") }
        } else {
            items(staffDrinks, key = { "staff-" + it.productName }) { item -> ProductSummaryCard(item) }
        }
        item { Text("Archivierte Tage", fontWeight = FontWeight.Bold) }
        if (archivedDays.isEmpty()) {
            item { Text("Noch kein Tagesabschluss archiviert") }
        } else {
            items(archivedDays, key = { it.dayKey }) { day -> ArchivedDayCard(day, onOpen = { onOpenArchivedDay(day.dayKey) }) }
        }
    }
}

@Composable
private fun ProductSummaryCard(item: ProductSalesSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.productName, fontWeight = FontWeight.SemiBold)
                Text("${item.quantity} Stück")
            }
            Text(item.revenueCents.toEuroString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ArchivedDayCard(day: ArchivedDay, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(day.dayKey, fontWeight = FontWeight.Bold)
            SummaryRow("Archiviert", day.closedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm")))
            SummaryRow("Umsatz", day.summary.revenueCents.toEuroString())
            SummaryRow("Verkäufe", day.summary.completedSalesCount.toString())
            SummaryRow("Personalgetränke", day.summary.staffSalesCount.toString())
            SummaryRow("Stornos", day.summary.cancelledSalesCount.toString())
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
