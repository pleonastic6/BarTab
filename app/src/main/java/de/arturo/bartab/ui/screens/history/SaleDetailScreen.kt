package de.arturo.bartab.ui.screens.history

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.SaleRecord
import de.arturo.bartab.state.SaleStatus
import de.arturo.bartab.ui.components.toEuroString
import java.time.format.DateTimeFormatter

private val SaleDetailTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm")

@Composable
fun SaleDetailScreen(
    sale: SaleRecord?,
    onBack: () -> Unit,
    onLoadIntoCart: (String) -> Unit,
    onCancelSale: (String) -> Unit,
) {
    if (sale == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Verkauf nicht gefunden", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(onClick = onBack) { Text("Zurück") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Verkaufsdetail", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(sale.createdAt.format(SaleDetailTimeFormatter), fontWeight = FontWeight.SemiBold)
                    Text("Status: ${sale.status.label}")
                    if (sale.isStaff) {
                        Text("Personalgetränk", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        if (sale.isStaff) "Dokumentiert ohne Umsatz" else "Summe: ${sale.totalCents.toEuroString()}",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item {
            Text("Positionen", fontWeight = FontWeight.Bold)
        }
        items(sale.items, key = { it.product.id + "-" + it.quantity }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.product.name, fontWeight = FontWeight.SemiBold)
                        Text("${item.quantity} Stück", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(item.lineTotalCents.toEuroString(), fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Button(
                onClick = {
                    onLoadIntoCart(sale.id)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = sale.status == SaleStatus.COMPLETED,
            ) {
                Text("Wieder öffnen")
            }
        }
        item {
            OutlinedButton(
                onClick = { onCancelSale(sale.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = sale.status == SaleStatus.COMPLETED,
            ) {
                Text("Verkauf stornieren")
            }
        }
        item {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Zurück")
            }
        }
    }
}
