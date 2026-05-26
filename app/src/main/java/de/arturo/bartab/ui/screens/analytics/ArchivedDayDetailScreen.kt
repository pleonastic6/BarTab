package de.arturo.bartab.ui.screens.analytics

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
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.state.ProductSalesSummary
import de.arturo.bartab.ui.components.toEuroString
import de.arturo.bartab.ui.export.shareCsv
import java.time.format.DateTimeFormatter

private val ArchivedDetailTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm")

@Composable
fun ArchivedDayDetailScreen(
    dayKey: String,
    state: BarTabViewModel,
    onBack: () -> Unit,
) {
    val archivedDay = state.archivedDayByKey(dayKey)
    val summary = state.summaryForArchivedDay(dayKey)
    val soldProducts = state.soldProductsForArchivedDay(dayKey)
    val staffDrinks = state.staffDrinksForArchivedDay(dayKey)
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(dayKey, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        shareCsv(
                            context = context,
                            fileName = state.exportFileNameForDay(dayKey),
                            content = state.buildCsvForDay(dayKey),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("CSV exportieren")
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Zurück")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Archiv", fontWeight = FontWeight.Bold)
                    if (archivedDay != null) {
                        SummaryRow("Abgeschlossen", archivedDay.closedAt.format(ArchivedDetailTimeFormatter))
                    }
                    SummaryRow("Umsatz", summary.revenueCents.toEuroString())
                    SummaryRow("Verkäufe", summary.completedSalesCount.toString())
                    SummaryRow("Personalgetränke", summary.staffSalesCount.toString())
                    SummaryRow("Stornos", summary.cancelledSalesCount.toString())
                }
            }
        }
        item { Text("Verkaufte Getränke", fontWeight = FontWeight.Bold) }
        if (soldProducts.isEmpty()) {
            item { EmptyArchiveCard("Keine bezahlten Verkäufe archiviert") }
        } else {
            items(soldProducts, key = { "sold-" + it.productName }) { item -> ProductSummaryCard(item) }
        }
        item { Text("Personalgetränke", fontWeight = FontWeight.Bold) }
        if (staffDrinks.isEmpty()) {
            item { EmptyArchiveCard("Keine Personalgetränke archiviert") }
        } else {
            items(staffDrinks, key = { "staff-" + it.productName }) { item -> ProductSummaryCard(item) }
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
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyArchiveCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
