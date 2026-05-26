package de.arturo.bartab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BarTabDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM products ORDER BY sortOrder ASC, name ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAtEpochMillis DESC")
    fun observeSalesWithItems(): Flow<List<SaleWithItemsEntity>>

    @Query("SELECT * FROM day_closures ORDER BY dayKey DESC")
    fun observeDayClosures(): Flow<List<DayClosureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayClosure(dayClosure: DayClosureEntity)

    @Query("UPDATE products SET active = :active WHERE id = :productId")
    suspend fun updateProductActive(productId: String, active: Boolean)

    @Query("UPDATE sales SET status = :status, updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: String, status: String, updatedAtEpochMillis: Long)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int
}
