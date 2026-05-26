package de.arturo.bartab.state

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.arturo.bartab.data.local.BarTabDatabase
import de.arturo.bartab.data.local.BarTabRepository
import de.arturo.bartab.data.model.Category
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.data.model.SaleItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class BarTabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BarTabRepository(BarTabDatabase.getInstance(application).barTabDao())

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var saleHistory by mutableStateOf<List<SaleRecord>>(emptyList())
        private set

    var selectedCategoryId by mutableStateOf("")
    private val cartMap = mutableStateMapOf<String, Int>()

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
        viewModelScope.launch {
            repository.observeCategories().collectLatest { newCategories ->
                categories = newCategories
                if (selectedCategoryId.isBlank()) {
                    selectedCategoryId = newCategories.firstOrNull()?.id.orEmpty()
                }
            }
        }
        viewModelScope.launch {
            repository.observeProducts().collectLatest { products = it }
        }
        viewModelScope.launch {
            repository.observeSales().collectLatest { saleHistory = it }
        }
    }

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
        if (current <= 1) cartMap.remove(productId) else cartMap[productId] = current - 1
    }

    fun clearCart() {
        cartMap.clear()
    }

    fun completeSale() {
        val items = cartItems
        if (items.isEmpty()) return
        viewModelScope.launch {
            repository.completeSale(items)
            cartMap.clear()
        }
    }

    fun setProductActive(productId: String, active: Boolean) {
        viewModelScope.launch { repository.setProductActive(productId, active) }
    }

    fun saveProduct(
        existingId: String?,
        name: String,
        priceCents: Int,
        categoryId: String,
        active: Boolean,
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || categoryId.isBlank()) return
        val existing = products.firstOrNull { it.id == existingId }
        val product = Product(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = trimmedName,
            priceCents = priceCents,
            categoryId = categoryId,
            active = active,
            sortOrder = existing?.sortOrder ?: (products.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0),
        )
        viewModelScope.launch { repository.upsertProduct(product) }
    }

    fun addCategory(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            sortOrder = categories.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0,
        )
        viewModelScope.launch { repository.addCategory(category) }
    }

    fun saleById(saleId: String): SaleRecord? = saleHistory.firstOrNull { it.id == saleId }

    fun loadSaleIntoCart(saleId: String): Boolean {
        val sale = saleById(saleId) ?: return false
        val nextCart = linkedMapOf<String, Int>()
        sale.items.forEach { item ->
            val currentProduct = products.firstOrNull { it.id == item.product.id && it.active }
            if (currentProduct != null) {
                nextCart[currentProduct.id] = (nextCart[currentProduct.id] ?: 0) + item.quantity
            }
        }
        if (nextCart.isEmpty()) return false
        cartMap.clear()
        nextCart.forEach { (productId, quantity) -> cartMap[productId] = quantity }
        val firstProduct = products.firstOrNull { it.id == nextCart.keys.first() }
        if (firstProduct != null) {
            selectedCategoryId = firstProduct.categoryId
        }
        return true
    }

    fun cancelSale(saleId: String) {
        viewModelScope.launch { repository.cancelSale(saleId) }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {}
    }
}

data class SaleRecord(
    val id: String,
    val createdAt: LocalDateTime,
    val items: List<SaleItem>,
    val status: SaleStatus,
) {
    val totalCents: Int = items.sumOf { it.lineTotalCents }
}

enum class SaleStatus(val label: String) {
    COMPLETED("abgeschlossen"),
    CANCELLED("storniert"),
}
