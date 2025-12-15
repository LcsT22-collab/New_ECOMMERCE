package pe.idat.apk_ecommerce.util

import pe.idat.apk_ecommerce.domain.model.Product


object CartManager {
    private val cart = mutableMapOf<Int, Product>()

    val items: List<Product> get() = cart.values.toList()
    val total: Double get() = cart.values.sumOf { it.price * it.quantity }
    val itemCount: Int get() = cart.values.sumOf { it.quantity }

    fun addToCart(product: Product, quantity: Int = 1): Boolean {
        val availableStock = product.stock

        return if (cart.containsKey(product.id)) {
            val existing = cart[product.id]!!
            val newTotalQty = existing.quantity + quantity

            if (newTotalQty <= availableStock) {
                existing.setQuantity(newTotalQty)
                existing.stock = availableStock
                true
            } else false
        } else {
            if (availableStock >= quantity) {
                cart[product.id] = product.copy().apply {
                    setQuantity(quantity)
                    stock = availableStock
                }
                true
            } else false
        }
    }

    fun updateCartItemQuantity(productId: Int, newQuantity: Int): Boolean {
        val item = cart[productId] ?: return false

        return when {
            newQuantity == 0 -> {
                cart.remove(productId)
                true
            }
            newQuantity <= item.stock -> {
                item.setQuantity(newQuantity)
                true
            }
            else -> false
        }
    }

    fun removeFromCart(product: Product) {
        cart.remove(product.id)
    }

    fun clearCart() = cart.clear()

    fun validateStockForPurchase(products: List<Product>): Boolean {
        return cart.values.all { cartItem ->
            products.find { it.id == cartItem.id }?.stock ?: 0 >= cartItem.quantity
        }
    }

    fun syncWithProducts(products: List<Product>) {
        cart.values.forEach { cartItem ->
            val updatedProduct = products.find { it.id == cartItem.id }
            updatedProduct?.let {
                cartItem.stock = it.stock

                if (cartItem.quantity > it.stock) {
                    cartItem.setQuantity(it.stock)
                }

                if (it.stock == 0) {
                    cart.remove(cartItem.id)
                }
            }
        }
    }

    fun getCartItem(productId: Int): Product? = cart[productId]
}