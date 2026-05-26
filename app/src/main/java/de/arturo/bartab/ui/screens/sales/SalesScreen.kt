package de.arturo.bartab.ui.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.state.BarTabState
import de.arturo.bartab.ui.components.toEuroString

@Composable
fun SalesScreen(state: BarTabState) {
    val categories = state.categories
    val products = state.productsForSelectedCategory()
    val cartItems = state.cartItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Verkauf", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                AssistChip(
                    onClick = { state.selectedCategoryId = category.id },
                    label = { Text(category.name) },
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(products) { product ->
                Card(onClick = { state.addProduct(product.id) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.priceCents.toEuroString())
                        val qty = cartItems.firstOrNull { it.product.id == product.id }?.quantity ?: 0
                        Text("Im Warenkorb: $qty")
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aktueller Warenkorb", fontWeight = FontWeight.Bold)
                if (cartItems.isEmpty()) {
                    Text("Noch keine Produkte ausgewählt")
                } else {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                                Text(item.lineTotalCents.toEuroString())
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { state.decrement(item.product.id) }) { Text("-") }
                                Text(item.quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                                OutlinedButton(onClick = { state.increment(item.product.id) }) { Text("+") }
                            }
                        }
                    }
                }
                Text("Summe: ${state.totalCents.toEuroString()}", fontWeight = FontWeight.Bold)
                Button(
                    onClick = { state.completeSale() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cartItems.isNotEmpty(),
                ) {
                    Text("Verkauf abschließen")
                }
            }
        }
    }
}
