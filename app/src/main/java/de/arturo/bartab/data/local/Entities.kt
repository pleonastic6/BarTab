package de.arturo.bartab.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId")],
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val priceCents: Int,
    val categoryId: String,
    val active: Boolean,
    val sortOrder: Int,
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val status: String,
    val totalCents: Int,
    val note: String?,
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("saleId")],
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: String,
    val productId: String,
    val productNameSnapshot: String,
    val unitPriceSnapshot: Int,
    val quantity: Int,
    val lineTotalCents: Int,
)

data class SaleWithItemsEntity(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId",
    )
    val items: List<SaleItemEntity>,
)
