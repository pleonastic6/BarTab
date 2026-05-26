package de.arturo.bartab.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.data.model.SaleItem
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.components.toEuroString

@Composable
fun SalesScreen(state: BarTabViewModel) {
    val categories = state.categories
    val products = state.productsForSelectedCategory()
    val cartItems = state.cartItems

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 900.dp

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(modifier = Modifier.weight(1.45f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SalesHeader(cartCount = cartItems.sumOf { it.quantity }, totalCents = state.totalCents)
                        CategoryRow(
                            categories = categories.map { it.id to it.name },
                            selectedCategoryId = state.selectedCategoryId,
                            onSelect = { state.selectedCategoryId = it },
                        )
                        ProductGrid(
                            products = products,
                            cartItems = cartItems,
                            onAddProduct = state::addProduct,
                            modifier = Modifier.weight(1f),
                            minCellSize = 170.dp,
                        )
                    }
                    CartPanel(
                        cartItems = cartItems,
                        totalCents = state.totalCents,
                        onIncrement = state::increment,
                        onDecrement = state::decrement,
                        onClear = state::clearCart,
                        onComplete = state::completeSale,
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SalesHeader(cartCount = cartItems.sumOf { it.quantity }, totalCents = state.totalCents)
                    CategoryRow(
                        categories = categories.map { it.id to it.name },
                        selectedCategoryId = state.selectedCategoryId,
                        onSelect = { state.selectedCategoryId = it },
                    )
                    ProductGrid(
                        products = products,
                        cartItems = cartItems,
                        onAddProduct = state::addProduct,
                        modifier = Modifier.weight(1f),
                        minCellSize = 135.dp,
                    )
                    CartPanel(
                        cartItems = cartItems,
                        totalCents = state.totalCents,
                        onIncrement = state::increment,
                        onDecrement = state::decrement,
                        onClear = state::clearCart,
                        onComplete = state::completeSale,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SalesHeader(cartCount: Int, totalCents: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Verkauf", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Schneller Tablet-Modus für den Ausschank",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("$cartCount Artikel", style = MaterialTheme.typography.titleMedium)
                Text(totalCents.toEuroString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Pair<String, String>>,
    selectedCategoryId: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        categories.forEach { (id, label) ->
            FilterChip(
                selected = selectedCategoryId == id,
                onClick = { onSelect(id) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    cartItems: List<SaleItem>,
    onAddProduct: (String) -> Unit,
    modifier: Modifier,
    minCellSize: androidx.compose.ui.unit.Dp,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minCellSize),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(products, key = { it.id }) { product ->
            val qty = cartItems.firstOrNull { it.product.id == product.id }?.quantity ?: 0
            ProductTile(
                product = product,
                quantityInCart = qty,
                onClick = { onAddProduct(product.id) },
            )
        }
    }
}

@Composable
private fun ProductTile(
    product: Product,
    quantityInCart: Int,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.heightIn(min = 132.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (quantityInCart > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            quantityInCart.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = product.priceCents.toEuroString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (quantityInCart > 0) "Zum Erhöhen tippen" else "Zum Hinzufügen tippen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CartPanel(
    cartItems: List<SaleItem>,
    totalCents: Int,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onClear: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Warenkorb", style = MaterialTheme.typography.titleLarge)
            if (cartItems.isEmpty()) {
                Text(
                    "Noch keine Produkte ausgewählt",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                cartItems.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    item.lineTotalCents.toEuroString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                QtyButton(label = "-", onClick = { onDecrement(item.product.id) })
                                Text(item.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                                QtyButton(label = "+", onClick = { onIncrement(item.product.id) })
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                    Text("Warenkorb leeren")
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Gesamtsumme", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(totalCents.toEuroString(), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
                }
            }
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                enabled = cartItems.isNotEmpty(),
            ) {
                Text("Verkauf abschließen")
            }
        }
    }
}

@Composable
private fun QtyButton(
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
