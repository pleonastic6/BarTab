package de.arturo.bartab.data.local

import de.arturo.bartab.data.model.Category
import de.arturo.bartab.data.model.Product
import de.arturo.bartab.data.model.SampleData
import de.arturo.bartab.data.model.SaleItem
import de.arturo.bartab.state.ArchivedDay
import de.arturo.bartab.state.DaySummary
import de.arturo.bartab.state.SaleRecord
import de.arturo.bartab.state.SaleStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class BarTabRepository(
    private val dao: BarTabDao,
) {
    fun observeCategories(): Flow<List<Category>> = dao.observeCategories().map { list ->
        list.map { entity -> Category(entity.id, entity.name, entity.sortOrder) }
    }

    fun observeProducts(): Flow<List<Product>> = dao.observeProducts().map { list ->
        list.map { entity ->
            Product(
                id = entity.id,
                name = entity.name,
                priceCents = entity.priceCents,
                categoryId = entity.categoryId,
                active = entity.active,
                quickAccess = entity.quickAccess,
                sortOrder = entity.sortOrder,
            )
        }
    }

    fun observeSales(): Flow<List<SaleRecord>> = dao.observeSalesWithItems().map { sales ->
        sales.map { saleWithItems ->
            SaleRecord(
                id = saleWithItems.sale.id,
                createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(saleWithItems.sale.createdAtEpochMillis),
                    ZoneId.systemDefault(),
                ),
                items = saleWithItems.items.map { item ->
                    SaleItem(
                        product = Product(
                            id = item.productId,
                            name = item.productNameSnapshot,
                            priceCents = item.unitPriceSnapshot,
                            categoryId = "snapshot",
                            active = true,
                            sortOrder = 0,
                        ),
                        quantity = item.quantity,
                    )
                },
                status = runCatching { SaleStatus.valueOf(saleWithItems.sale.status) }
                    .getOrDefault(SaleStatus.COMPLETED),
                isStaff = saleWithItems.sale.isStaff,
            )
        }
    }

    fun observeDayClosures(): Flow<List<ArchivedDay>> = dao.observeDayClosures().map { closures ->
        closures.map { entity ->
            ArchivedDay(
                dayKey = entity.dayKey,
                closedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.closedAtEpochMillis),
                    ZoneId.systemDefault(),
                ),
                summary = DaySummary(
                    completedSalesCount = entity.completedSalesCount,
                    cancelledSalesCount = entity.cancelledSalesCount,
                    staffSalesCount = entity.staffSalesCount,
                    revenueCents = entity.revenueCents,
                ),
            )
        }
    }

    suspend fun seedIfEmpty() {
        if (dao.categoryCount() > 0) return
        dao.insertCategories(SampleData.categories.map { CategoryEntity(it.id, it.name, it.sortOrder) })
        dao.insertProducts(
            SampleData.products.map {
                ProductEntity(
                    id = it.id,
                    name = it.name,
                    priceCents = it.priceCents,
                    categoryId = it.categoryId,
                    active = it.active,
                    quickAccess = it.quickAccess,
                    sortOrder = it.sortOrder,
                )
            },
        )
    }

    suspend fun completeSale(items: List<SaleItem>, isStaff: Boolean) {
        if (items.isEmpty()) return
        val saleId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val total = items.sumOf { it.lineTotalCents }

        dao.insertSale(
            SaleEntity(
                id = saleId,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                status = SaleStatus.COMPLETED.name,
                totalCents = total,
                isStaff = isStaff,
                note = null,
            ),
        )
        dao.insertSaleItems(
            items.map {
                SaleItemEntity(
                    saleId = saleId,
                    productId = it.product.id,
                    productNameSnapshot = it.product.name,
                    unitPriceSnapshot = it.product.priceCents,
                    quantity = it.quantity,
                    lineTotalCents = it.lineTotalCents,
                )
            },
        )
    }

    suspend fun archiveDay(dayKey: String, summary: DaySummary) {
        dao.insertDayClosure(
            DayClosureEntity(
                dayKey = dayKey,
                closedAtEpochMillis = System.currentTimeMillis(),
                revenueCents = summary.revenueCents,
                completedSalesCount = summary.completedSalesCount,
                staffSalesCount = summary.staffSalesCount,
                cancelledSalesCount = summary.cancelledSalesCount,
            ),
        )
    }

    suspend fun setProductActive(productId: String, active: Boolean) {
        dao.updateProductActive(productId, active)
    }

    suspend fun upsertProduct(product: Product) {
        dao.insertProduct(
            ProductEntity(
                id = product.id,
                name = product.name,
                priceCents = product.priceCents,
                categoryId = product.categoryId,
                active = product.active,
                quickAccess = product.quickAccess,
                sortOrder = product.sortOrder,
            ),
        )
    }

    suspend fun addCategory(category: Category) {
        dao.insertCategory(CategoryEntity(category.id, category.name, category.sortOrder))
    }

    suspend fun cancelSale(saleId: String) {
        dao.updateSaleStatus(
            saleId = saleId,
            status = SaleStatus.CANCELLED.name,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }
}
