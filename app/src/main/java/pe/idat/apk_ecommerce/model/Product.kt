package pe.idat.apk_ecommerce.domain.model

import java.io.Serializable

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    var stock: Int,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    var isFavorite: Boolean = false
) : Serializable {
    var quantity: Int = 1
        private set

    fun setQuantity(qty: Int) {
        quantity = qty.coerceAtLeast(1).coerceAtMost(stock)
    }

    fun totalPrice(): Double = price * quantity
}