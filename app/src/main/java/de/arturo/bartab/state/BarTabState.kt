package de.arturo.bartab.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.data.model.SampleData
import de.arturo.bartab.data.model.SaleItem
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class SaleRecord(
    val id: String,
    val createdAt: LocalDateTime,
    val items: List<SaleItem>,
    val status: SaleStatus,
) {
    val totalCents: Int = items.sumOf { it.lineTotalCents }

    fun summary(): String {
        val time = createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))
        return "$time · ${totalCents.toEuroString()} · ${status.label}"
    }
}

enum class SaleStatus(val label: String) {
    COMPLETED("abgeschlossen"),
    CANCELLED("storniert"),
}

class BarTabState {
    val categories = SampleData.categories
    val products = SampleData.products

    var selectedCategoryId by mutableStateOf(categories.firstOrNull()?.id.orEmpty())
    private val cartMap = mutableStateMapOf<String, Int>()
    val saleHistory = mutableStateListOf<SaleRecord>()

    val cartItems: List<SaleItem>
        get() = cartMap.entries.mapNotNull { entry ->
            val product = products.find { it.id == entry.key } ?: return@mapNotNull null
            SaleItem(product = product, quantity = entry.value)
        }.sortedBy { it.product.sortOrder }

    val totalCents: Int
        get() = cartItems.sumOf { it.lineTotalCents }

    fun productsForSelectedCategory(): List<Product> =
        products.filter { it.categoryId == selectedCategoryId && it.active }.sortedBy { it.sortOrder }

    fun addProduct(productId: String) {
        cartMap[productId] = (cartMap[productId] ?: 0) + 1
    }

    fun increment(productId: String) = addProduct(productId)

    fun decrement(productId: String) {
        val current = cartMap[productId] ?: return
        if (current <= 1) {
            cartMap.remove(productId)
        } else {
            cartMap[productId] = current - 1
        }
    }

    fun remove(productId: String) {
        cartMap.remove(productId)
    }

    fun completeSale() {
        if (cartItems.isEmpty()) return
        saleHistory.add(
            0,
            SaleRecord(
                id = UUID.randomUUID().toString(),
                createdAt = LocalDateTime.now(),
                items = cartItems,
                status = SaleStatus.COMPLETED,
            ),
        )
        cartMap.clear()
    }
}

private fun Int.toEuroString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return format.format(this / 100.0)
}
