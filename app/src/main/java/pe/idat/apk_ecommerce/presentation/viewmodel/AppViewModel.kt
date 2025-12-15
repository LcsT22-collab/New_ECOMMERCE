package pe.idat.apk_ecommerce.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.domain.model.Product
import pe.idat.apk_ecommerce.util.CartManager


class AppViewModel(private val repository: AppRepository) : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _favoriteProducts = MutableLiveData<List<Product>>()
    val favoriteProducts: LiveData<List<Product>> = _favoriteProducts

    private val _cartItems = MutableLiveData<List<Product>>()
    val cartItems: LiveData<List<Product>> = _cartItems

    private val _purchaseResult = MutableLiveData<Pair<Boolean, String?>>()
    val purchaseResult: LiveData<Pair<Boolean, String?>> = _purchaseResult

    // Agregar TAG para logs
    private companion object {
        const val TAG = "AppViewModel"
    }

    init {
        Log.d(TAG, "ViewModel inicializado")
        loadProductsFromLocal()
        updateCart()
    }

    fun loadProductsFromLocal() {
        Log.d(TAG, "Cargando productos desde almacenamiento local")
        viewModelScope.launch {
            repository.getProductsFromLocal().onSuccess { products ->
                Log.d(TAG, "Productos locales cargados: ${products.size} items")

                // CORRECCIÓN: Si no hay productos locales, cargar de la API
                if (products.isEmpty()) {
                    Log.d(TAG, "No hay productos locales, cargando desde API...")
                    loadProductsFromApi()
                } else {
                    CartManager.syncWithProducts(products)
                    _products.value = products
                    updateCart()
                    loadFavoriteProducts()
                }
            }.onFailure { exception ->
                Log.e(TAG, "Error al cargar productos locales: ${exception.message}")
                Log.d(TAG, "Intentando cargar desde API...")
                loadProductsFromApi()
            }
        }
    }

    fun loadProductsFromApi() {
        Log.d(TAG, "Cargando productos desde API")
        viewModelScope.launch {
            repository.loadProductsFromApi().onSuccess { products ->
                Log.d(TAG, "Productos de API cargados exitosamente: ${products.size} items")
                _products.value = products
                loadFavoriteProducts()
                CartManager.syncWithProducts(products)
                updateCart()
            }.onFailure { exception ->
                Log.e(TAG, "Error fatal: No se pudieron cargar productos ni de API ni locales: ${exception.message}")

                // CORRECCIÓN: Datos de prueba si la API falla
                val testProducts = createTestProducts()
                _products.value = testProducts
                CartManager.syncWithProducts(testProducts)
                loadFavoriteProducts()
                updateCart()
            }
        }
    }
    private fun createTestProducts(): List<Product> {
        Log.d(TAG, "Creando productos de prueba")
        return listOf(
            Product(
                id = 1,
                name = "Laptop Gamer ASUS",
                price = 2999.99,
                description = "Laptop para gaming con RTX 4060",
                category = "Electrónica",
                image = "https://cdn.pixabay.com/photo/2014/09/27/13/44/notebook-463485_960_720.jpg",
                stock = 10,
                rating = 4.5f,
                reviewCount = 120
            ),
            Product(
                id = 2,
                name = "Mouse Inalámbrico Logitech",
                price = 49.99,
                description = "Mouse ergonómico con sensor óptico",
                category = "Electrónica",
                image = "https://cdn.pixabay.com/photo/2013/07/13/11/44/mouse-158823_960_720.png",
                stock = 25,
                rating = 4.2f,
                reviewCount = 85
            ),
            Product(
                id = 3,
                name = "Teclado Mecánico Redragon",
                price = 89.99,
                description = "Teclado mecánico con switches azules",
                category = "Electrónica",
                image = "https://cdn.pixabay.com/photo/2014/09/27/13/46/keyboard-463525_960_720.jpg",
                stock = 15,
                rating = 4.7f,
                reviewCount = 200
            )
        )
    }

    fun loadFavoriteProducts() {
        Log.d(TAG, "Cargando productos favoritos")
        viewModelScope.launch {
            val favorites = repository.getFavoriteProducts()
            Log.d(TAG, "Productos favoritos encontrados: ${favorites.size}")
            _favoriteProducts.value = favorites
        }
    }



    fun addToCart(product: Product, quantity: Int = 1): Boolean {
        return if (product.stock >= quantity) {
            CartManager.addToCart(product, quantity).also { updateCart() }
        } else false
    }

    fun updateCartItemQuantity(productId: Int, newQuantity: Int): Boolean {
        return CartManager.updateCartItemQuantity(productId, newQuantity).also {
            if (it) updateCart()
        }
    }

    fun removeFromCart(product: Product) {
        CartManager.removeFromCart(product)
        updateCart()
    }

    fun updateCart() {
        _cartItems.value = CartManager.items
    }

    fun processPurchase() {
        viewModelScope.launch {
            Log.d(TAG, "Procesando compra...")
            val cartItems = CartManager.items
            Log.d(TAG, "Items en carrito: ${cartItems.size}")

            if (cartItems.isEmpty()) {
                Log.d(TAG, "Carrito vacío, no se puede procesar compra")
                _purchaseResult.value = Pair(false, "Carrito vacío")
                return@launch
            }

            val currentProducts = _products.value ?: emptyList()
            Log.d(TAG, "Productos actuales: ${currentProducts.size}")

            if (!CartManager.validateStockForPurchase(currentProducts)) {
                Log.d(TAG, "Stock insuficiente para la compra")
                _purchaseResult.value = Pair(false, "Stock insuficiente")
                return@launch
            }

            val updatedProducts = currentProducts.map { product ->
                cartItems.find { it.id == product.id }?.let { cartItem ->
                    product.copy(stock = product.stock - cartItem.quantity)
                } ?: product
            }

            Log.d(TAG, "Actualizando productos en repositorio...")
            if (repository.updateProducts(updatedProducts)) {
                Log.d(TAG, "Productos actualizados exitosamente")
                _products.value = updatedProducts
                CartManager.syncWithProducts(updatedProducts)
                CartManager.clearCart()
                updateCart()
                _purchaseResult.value = Pair(true, "Compra realizada exitosamente")
            } else {
                Log.e(TAG, "Error al guardar cambios en repositorio")
                _purchaseResult.value = Pair(false, "Error al guardar cambios")
            }
        }
    }

    fun login(email: String, password: String, onResult: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.login(email, password))
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.register(name, email, password))
        }
    }

    fun logout() {
        CartManager.clearCart()
        repository.logout()
    }
    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(productId)
            _products.value = _products.value?.map {
                if (it.id == productId) it.copy(isFavorite = isNowFavorite) else it
            }
            loadFavoriteProducts()
        }
    }

    fun getCurrentUser() = repository.getCurrentUser()
}