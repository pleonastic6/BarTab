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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class BarTabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BarTabRepository(BarTabDatabase.getInstance(application).barTabDao())

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var saleHistory by mutableStateOf<List<SaleRecord>>(emptyList())
        private set
    var archivedDays by mutableStateOf<List<ArchivedDay>>(emptyList())
        private set
    var currentSaleIsStaff by mutableStateOf(false)

    private val cartMap = mutableStateMapOf<String, Int>()

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
        viewModelScope.launch { repository.observeCategories().collectLatest { categories = it } }
        viewModelScope.launch { repository.observeProducts().collectLatest { products = it } }
        viewModelScope.launch { repository.observeSales().collectLatest { saleHistory = it } }
        viewModelScope.launch { repository.observeDayClosures().collectLatest { archivedDays = it } }
    }

    val cartItems: List<SaleItem>
        get() = cartMap.entries.mapNotNull { entry ->
            val product = products.find { it.id == entry.key } ?: return@mapNotNull null
            SaleItem(product = product, quantity = entry.value)
        }.sortedBy { it.product.sortOrder }

    val totalCents: Int
        get() = cartItems.sumOf { it.lineTotalCents }

    private fun salesForDay(dayKey: String): List<SaleRecord> =
        saleHistory.filter { it.createdAt.toLocalDate().toString() == dayKey }

    private fun summaryForDay(dayKey: String): DaySummary {
        val daySales = salesForDay(dayKey)
        val completedSales = daySales.filter { it.status == SaleStatus.COMPLETED && !it.isStaff }
        val cancelledSales = daySales.filter { it.status == SaleStatus.CANCELLED }
        val staffSales = daySales.filter { it.status == SaleStatus.COMPLETED && it.isStaff }
        return DaySummary(
            completedSalesCount = completedSales.size,
            cancelledSalesCount = cancelledSales.size,
            staffSalesCount = staffSales.size,
            revenueCents = completedSales.sumOf { it.totalCents },
        )
    }

    private fun productSummariesForDay(dayKey: String, staff: Boolean): List<ProductSalesSummary> =
        salesForDay(dayKey)
            .filter { it.status == SaleStatus.COMPLETED && it.isStaff == staff }
            .flatMap { it.items }
            .groupBy { it.product.name }
            .map { (productName, items) ->
                ProductSalesSummary(
                    productName = productName,
                    quantity = items.sumOf { it.quantity },
                    revenueCents = items.sumOf { it.lineTotalCents },
                )
            }
            .sortedWith(compareByDescending<ProductSalesSummary> { it.quantity }.thenBy { it.productName })

    val todayKey: String
        get() = LocalDate.now().toString()

    val todaySummary: DaySummary
        get() = summaryForDay(todayKey)

    val todayProductSummaries: List<ProductSalesSummary>
        get() = productSummariesForDay(todayKey, staff = false)

    val todayStaffDrinkSummaries: List<ProductSalesSummary>
        get() = productSummariesForDay(todayKey, staff = true)

    val isTodayArchived: Boolean
        get() = archivedDays.any { it.dayKey == todayKey }

    fun archivedDayByKey(dayKey: String): ArchivedDay? = archivedDays.firstOrNull { it.dayKey == dayKey }

    fun summaryForArchivedDay(dayKey: String): DaySummary = archivedDayByKey(dayKey)?.summary ?: summaryForDay(dayKey)

    fun soldProductsForArchivedDay(dayKey: String): List<ProductSalesSummary> = productSummariesForDay(dayKey, staff = false)

    fun staffDrinksForArchivedDay(dayKey: String): List<ProductSalesSummary> = productSummariesForDay(dayKey, staff = true)

    fun archiveToday() {
        if (isTodayArchived) return
        viewModelScope.launch { repository.archiveDay(todayKey, todaySummary) }
        clearCart()
    }

    fun buildTodayCsv(): String = buildCsvForDay(todayKey)

    fun buildCsvForDay(dayKey: String): String {
        val summary = summaryForArchivedDay(dayKey)
        val daySales = salesForDay(dayKey)
        val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val builder = StringBuilder()
        builder.appendLine("BarTab Tagesexport;$dayKey")
        builder.appendLine("Umsatz;${summary.revenueCents.toEuroDecimal()}")
        builder.appendLine("Verkäufe;${summary.completedSalesCount}")
        builder.appendLine("Personalgetränke;${summary.staffSalesCount}")
        builder.appendLine("Stornos;${summary.cancelledSalesCount}")
        builder.appendLine()

        builder.appendLine("Verkaufte Getränke")
        builder.appendLine("sale_id;zeit;status;produkt;menge;einzelpreis_eur;zeilensumme_eur")
        daySales
            .filter { it.status == SaleStatus.COMPLETED && !it.isStaff }
            .forEach { sale ->
                sale.items.forEach { item ->
                    builder.appendLine(
                        listOf(
                            sale.id,
                            sale.createdAt.format(timeFormat),
                            sale.status.name,
                            item.product.name.csvEscape(),
                            item.quantity.toString(),
                            item.product.priceCents.toEuroDecimal(),
                            item.lineTotalCents.toEuroDecimal(),
                        ).joinToString(";"),
                    )
                }
            }

        builder.appendLine()
        builder.appendLine("Personalgetränke")
        builder.appendLine("sale_id;zeit;status;produkt;menge;einzelpreis_eur;zeilensumme_eur")
        daySales
            .filter { it.status == SaleStatus.COMPLETED && it.isStaff }
            .forEach { sale ->
                sale.items.forEach { item ->
                    builder.appendLine(
                        listOf(
                            sale.id,
                            sale.createdAt.format(timeFormat),
                            sale.status.name,
                            item.product.name.csvEscape(),
                            item.quantity.toString(),
                            item.product.priceCents.toEuroDecimal(),
                            item.lineTotalCents.toEuroDecimal(),
                        ).joinToString(";"),
                    )
                }
            }

        return builder.toString()
    }

    fun exportFileName(): String = "bartab-export-$todayKey.csv"

    fun exportFileNameForDay(dayKey: String): String = "bartab-export-$dayKey.csv"

    fun activeProductsForCategory(categoryId: String): List<Product> =
        products.filter { it.categoryId == categoryId && it.active }.sortedBy { it.sortOrder }

    fun quickAccessProductsForCategory(categoryId: String): List<Product> {
        val inCategory = activeProductsForCategory(categoryId)
        val favorites = inCategory.filter { it.quickAccess }.take(5)
        return if (favorites.isNotEmpty()) favorites else inCategory.take(5)
    }

    fun addProduct(productId: String) {
        if (isTodayArchived) return
        cartMap[productId] = (cartMap[productId] ?: 0) + 1
    }

    fun increment(productId: String) = addProduct(productId)

    fun decrement(productId: String) {
        val current = cartMap[productId] ?: return
        if (current <= 1) cartMap.remove(productId) else cartMap[productId] = current - 1
    }

    fun clearCart() {
        cartMap.clear()
        currentSaleIsStaff = false
    }

    fun completeSale() {
        if (isTodayArchived) return
        val items = cartItems
        if (items.isEmpty()) return
        val isStaff = currentSaleIsStaff
        viewModelScope.launch {
            repository.completeSale(items, isStaff)
            cartMap.clear()
            currentSaleIsStaff = false
        }
    }

    fun toggleCurrentSaleIsStaff(value: Boolean) {
        if (isTodayArchived) return
        currentSaleIsStaff = value
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
        quickAccess: Boolean,
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
            quickAccess = quickAccess,
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
        if (isTodayArchived) return false
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
        currentSaleIsStaff = sale.isStaff
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
    val isStaff: Boolean,
) {
    val totalCents: Int = items.sumOf { it.lineTotalCents }
}

data class DaySummary(
    val completedSalesCount: Int,
    val cancelledSalesCount: Int,
    val staffSalesCount: Int,
    val revenueCents: Int,
)

data class ProductSalesSummary(
    val productName: String,
    val quantity: Int,
    val revenueCents: Int,
)

data class ArchivedDay(
    val dayKey: String,
    val closedAt: LocalDateTime,
    val summary: DaySummary,
)

enum class SaleStatus(val label: String) {
    COMPLETED("abgeschlossen"),
    CANCELLED("storniert"),
}

private fun Int.toEuroDecimal(): String = "%.2f".format(this / 100.0).replace('.', ',')
private fun String.csvEscape(): String = '"' + replace("\"", "\"\"") + '"'
