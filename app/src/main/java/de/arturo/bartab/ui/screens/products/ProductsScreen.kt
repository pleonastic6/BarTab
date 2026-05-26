package de.arturo.bartab.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.state.BarTabViewModel
import de.arturo.bartab.ui.components.toEuroString

@Composable
fun ProductsScreen(state: BarTabViewModel) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Produkte", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showCategoryDialog = true }) { Text("Kategorie") }
                Button(onClick = { showCreateDialog = true }) { Text("Neu") }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.products, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { editingProduct = product },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            Text(product.priceCents.toEuroString())
                            Text("Kategorie: ${state.categories.firstOrNull { it.id == product.categoryId }?.name ?: "-"}")
                            if (product.quickAccess) {
                                Text("Hotbar aktiv", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Switch(
                            checked = product.active,
                            onCheckedChange = { checked -> state.setProductActive(product.id, checked) },
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ProductDialog(
            title = "Produkt anlegen",
            categories = state.categories,
            initialProduct = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, priceCents, categoryId, active, quickAccess ->
                state.saveProduct(null, name, priceCents, categoryId, active, quickAccess)
                showCreateDialog = false
            },
        )
    }

    editingProduct?.let { product ->
        ProductDialog(
            title = "Produkt bearbeiten",
            categories = state.categories,
            initialProduct = product,
            onDismiss = { editingProduct = null },
            onSave = { name, priceCents, categoryId, active, quickAccess ->
                state.saveProduct(product.id, name, priceCents, categoryId, active, quickAccess)
                editingProduct = null
            },
        )
    }

    if (showCategoryDialog) {
        CategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = { name ->
                state.addCategory(name)
                showCategoryDialog = false
            },
        )
    }
}

@Composable
private fun ProductDialog(
    title: String,
    categories: List<de.arturo.bartab.data.model.Category>,
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, priceCents: Int, categoryId: String, active: Boolean, quickAccess: Boolean) -> Unit,
) {
    var name by remember(initialProduct) { mutableStateOf(initialProduct?.name.orEmpty()) }
    var priceEuros by remember(initialProduct) {
        mutableStateOf(
            initialProduct?.let { "%.2f".format(it.priceCents / 100.0).replace('.', ',') }.orEmpty(),
        )
    }
    var categoryId by remember(initialProduct, categories) {
        mutableStateOf(initialProduct?.categoryId ?: categories.firstOrNull()?.id.orEmpty())
    }
    var active by remember(initialProduct) { mutableStateOf(initialProduct?.active ?: true) }
    var quickAccess by remember(initialProduct) { mutableStateOf(initialProduct?.quickAccess ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = priceEuros,
                    onValueChange = { priceEuros = it },
                    label = { Text("Preis in €") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Text("Kategorie", fontWeight = FontWeight.SemiBold)
                categories.forEach { category ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = categoryId == category.id, onClick = { categoryId = category.id })
                        Text(
                            text = category.name,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("In Hotbar", modifier = Modifier.padding(top = 12.dp))
                    Switch(checked = quickAccess, onCheckedChange = { quickAccess = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Aktiv", modifier = Modifier.padding(top = 12.dp))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalized = priceEuros.replace(',', '.')
                    val cents = (normalized.toDoubleOrNull()?.times(100))?.toInt() ?: return@Button
                    onSave(name, cents, categoryId, active, quickAccess)
                },
                enabled = name.isNotBlank() && categoryId.isNotBlank() && categories.isNotEmpty(),
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}

@Composable
private fun CategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategorie anlegen") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name) }, enabled = name.isNotBlank()) {
                Text("Speichern")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
