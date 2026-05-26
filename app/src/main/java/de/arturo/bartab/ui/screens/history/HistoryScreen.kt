package de.arturo.bartab.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.components.toEuroString
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    state: BarTabViewModel,
    onSaleClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Historie", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        if (state.saleHistory.isEmpty()) {
            Text("Noch keine Verkäufe gespeichert")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.saleHistory, key = { it.id }) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSaleClick(sale.id) },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${sale.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm"))} · ${sale.status.label}",
                                fontWeight = FontWeight.SemiBold,
                            )
                            sale.items.forEach { item ->
                                Text("${item.quantity}× ${item.product.name} · ${item.lineTotalCents.toEuroString()}")
                            }
                            Text("Summe: ${sale.totalCents.toEuroString()}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
