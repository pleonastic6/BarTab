package de.arturo.bartab.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.arturo.bartab.data.model.Category
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.data.model.SaleItem
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.components.toEuroString

@Composable
fun SalesScreen(state: BarTabViewModel) {
    val categories = state.categories
    val cartItems = state.cartItems
    var expandedCategory by remember { mutableStateOf<Category?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 1000.dp

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CategoryHotbarList(
                        categories = categories,
                        state = state,
                        onOpenCategory = { expandedCategory = it },
                        modifier = Modifier.weight(1.25f),
                    )
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CategoryHotbarList(
                        categories = categories,
                        state = state,
                        onOpenCategory = { expandedCategory = it },
                        modifier = Modifier.weight(1f),
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

    expandedCategory?.let { category ->
        CategoryItemsDialog(
            category = category,
            products = state.activeProductsForCategory(category.id),
            cartItems = cartItems,
            onDismiss = { expandedCategory = null },
            onAddProduct = state::addProduct,
        )
    }
}

@Composable
private fun CategoryHotbarList(
    categories: List<Category>,
    state: BarTabViewModel,
    onOpenCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories, key = { it.id }) { category ->
                    val hotbarProducts = state.quickAccessProductsForCategory(category.id)
                    CategoryRowCard(
                        category = category,
                        hotbarProducts = hotbarProducts,
                        onOpenCategory = { onOpenCategory(category) },
                        onAddProduct = state::addProduct,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRowCard(
    category: Category,
    hotbarProducts: List<Product>,
    onOpenCategory: () -> Unit,
    onAddProduct: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .clickable(onClick = onOpenCategory)
                    .heightIn(min = 110.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(category.name, style = MaterialTheme.typography.titleLarge)                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f).heightIn(min = 92.dp, max = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                items(hotbarProducts, key = { it.id }) { product ->
                    HotbarTile(product = product, onClick = { onAddProduct(product.id) })
                }
            }
        }
    }
}

@Composable
private fun HotbarTile(
    product: Product,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.heightIn(min = 110.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                product.priceCents.toEuroString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CategoryItemsDialog(
    category: Category,
    products: List<Product>,
    cartItems: List<SaleItem>,
    onDismiss: () -> Unit,
    onAddProduct: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(category.name) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 520.dp),
            ) {
                items(products, key = { it.id }) { product ->
                    val qty = cartItems.firstOrNull { it.product.id == product.id }?.quantity ?: 0
                    ProductTile(product = product, quantityInCart = qty, onClick = { onAddProduct(product.id) })
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Schließen") }
        },
    )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.heightIn(min = 148.dp),
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
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
