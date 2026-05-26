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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.data.model.SampleData
import de.arturo.bartab.ui.components.toEuroString

@Composable
fun SalesScreen() {
    val cart = remember { mutableStateMapOf<String, Int>() }
    val categories = SampleData.categories
    val selectedCategory = remember { androidx.compose.runtime.mutableStateOf(categories.first().id) }
    val products = SampleData.products.filter { it.categoryId == selectedCategory.value }
    val total = cart.entries.sumOf { entry ->
        val product = SampleData.products.first { it.id == entry.key }
        product.priceCents * entry.value
    }

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
                    onClick = { selectedCategory.value = category.id },
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
                Card(onClick = { cart[product.id] = (cart[product.id] ?: 0) + 1 }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.priceCents.toEuroString())
                        Text("Im Warenkorb: ${cart[product.id] ?: 0}")
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aktueller Warenkorb", fontWeight = FontWeight.Bold)
                if (cart.isEmpty()) {
                    Text("Noch keine Produkte ausgewählt")
                } else {
                    cart.entries.forEach { entry ->
                        val product = SampleData.products.first { it.id == entry.key }
                        Text("${entry.value}× ${product.name} · ${(product.priceCents * entry.value).toEuroString()}")
                    }
                }
                Text("Summe: ${total.toEuroString()}", fontWeight = FontWeight.Bold)
                Button(onClick = { /* next step: persist sale */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Verkauf abschließen")
                }
            }
        }
    }
}
