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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.components.toEuroString
import de.arturo.bartab.ui.export.shareCsv

@Composable
fun AnalyticsScreen(state: BarTabViewModel) {
    val summary = state.todaySummary
    val soldProducts = state.todayProductSummaries
    val staffDrinks = state.todayStaffDrinkSummaries
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Auswertung", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Button(
            onClick = {
                shareCsv(
                    context = context,
                    fileName = state.exportFileName(),
                    content = state.buildTodayCsv(),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("CSV exportieren")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Heute", fontWeight = FontWeight.Bold)
                SummaryRow("Umsatz", summary.revenueCents.toEuroString())
                SummaryRow("Verkäufe", summary.completedSalesCount.toString())
                SummaryRow("Personalgetränke", summary.staffSalesCount.toString())
                SummaryRow("Stornos", summary.cancelledSalesCount.toString())
            }
        }

        Text("Verkaufte Getränke", fontWeight = FontWeight.Bold)
        if (soldProducts.isEmpty()) {
            Text("Heute wurden noch keine bezahlten Verkäufe abgeschlossen")
        } else {
            ProductSummaryList(items = soldProducts)
        }

        Text("Personalgetränke", fontWeight = FontWeight.Bold)
        if (staffDrinks.isEmpty()) {
            Text("Heute wurden noch keine Personalgetränke dokumentiert")
        } else {
            ProductSummaryList(items = staffDrinks)
        }
    }
}

@Composable
private fun ProductSummaryList(items: List<de.arturo.bartab.state.ProductSalesSummary>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.productName }) { item ->
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
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
