package de.arturo.bartab.data.model

data class Category(
    val id: String,
    val name: String,
    val sortOrder: Int,
)

data class Product(
    val id: String,
    val name: String,
    val priceCents: Int,
    val categoryId: String,
    val active: Boolean = true,
    val sortOrder: Int = 0,
)

data class SaleItem(
    val product: Product,
    val quantity: Int,
) {
    val lineTotalCents: Int
        get() = product.priceCents * quantity
}
