package de.arturo.bartab.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.state.SaleRecord
import de.arturo.bartab.state.SaleStatus
import de.arturo.bartab.ui.components.toEuroString
import java.time.format.DateTimeFormatter

private val HistoryTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm")

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
            EmptyHistoryCard()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.saleHistory, key = { it.id }) { sale ->
                    HistorySaleCard(sale = sale, onClick = { onSaleClick(sale.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Noch keine Verkäufe gespeichert", fontWeight = FontWeight.SemiBold)
            Text(
                "Abgeschlossene Verkäufe und Personalgetränke tauchen hier auf.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistorySaleCard(
    sale: SaleRecord,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        sale.createdAt.format(HistoryTimeFormatter),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(sale)
                        if (sale.isStaff) {
                            NeutralPill("Personal")
                        }
                    }
                }

                Text(
                    if (sale.isStaff) "ohne Umsatz" else sale.totalCents.toEuroString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            sale.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${item.quantity}× ${item.product.name}")
                    Text(item.lineTotalCents.toEuroString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (sale.items.size > 3) {
                Text(
                    "+ ${sale.items.size - 3} weitere Positionen",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(sale: SaleRecord) {
    val containerColor = when (sale.status) {
        SaleStatus.COMPLETED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    }
    val contentColor = when (sale.status) {
        SaleStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(sale.status.label, color = contentColor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun NeutralPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
