package de.arturo.bartab.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Verkaufsdetail", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(sale.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm")), fontWeight = FontWeight.SemiBold)
                Text("Status: ${sale.status.label}")
                if (sale.isStaff) {
                    Text("Personalgetränk", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                sale.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}× ${item.product.name}")
                        Text(item.lineTotalCents.toEuroString())
                    }
                }
                Text(
                    if (sale.isStaff) "Dokumentiert ohne Umsatz" else "Summe: ${sale.totalCents.toEuroString()}",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
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
        OutlinedButton(
            onClick = { onCancelSale(sale.id) },
            modifier = Modifier.fillMaxWidth(),
            enabled = sale.status == SaleStatus.COMPLETED,
        ) {
            Text("Verkauf stornieren")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
    }
}
