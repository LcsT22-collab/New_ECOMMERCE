package pe.idat.apk_ecommerce.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import coil.load
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityProductDetailBinding
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.domain.model.Product
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory
import pe.idat.apk_ecommerce.util.CartManager

class ProductDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private var currentProduct: Product? = null
    private lateinit var viewModel: AppViewModel

    // Agregar TAG para logs
    private companion object {
        const val TAG = "ProductDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "ProductDetailActivity creado")

        viewModel = ViewModelProvider(this, AppViewModelFactory(AppRepository(applicationContext)))[AppViewModel::class.java]

        setupListeners()
        setupObservers()
        loadProductDetails()
    }

    private fun setupObservers() {
        viewModel.products.observe(this) { products ->
            Log.d(TAG, "Observador de productos: recibidos ${products.size} productos")
            currentProduct?.let { oldProduct ->
                products.find { it.id == oldProduct.id }?.let { newProduct ->
                    Log.d(TAG, "Producto actualizado: ${newProduct.name}, favorito: ${newProduct.isFavorite}")
                    currentProduct = newProduct
                    updateStockDisplay(newProduct.stock)
                    updateFavoriteButton(newProduct.isFavorite)
                    updateRatingDisplay(newProduct.rating)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackDetail.setOnClickListener {
            Log.d(TAG, "Botón retroceso presionado")
            onBackPressed()
        }
        binding.btnAddToCartDetail.setOnClickListener {
            Log.d(TAG, "Agregar al carrito presionado")
            currentProduct?.let { addToCart(it) }
        }
        binding.btnFavoriteDetail.setOnClickListener {
            currentProduct?.let { product ->
                Log.d(TAG, "Botón favorito presionado para: ${product.name} (ID: ${product.id})")
                // CORRECCIÓN: Pasar solo el ID del producto
                viewModel.toggleFavorite(product.id)
                Toast.makeText(this,
                    if (product.isFavorite) getString(R.string.removed_from_favorites)
                    else getString(R.string.added_to_favorites),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProductDetails() {
        currentProduct = intent.getSerializableExtra("product") as? Product

        currentProduct?.let { product ->
            Log.d(TAG, "Cargando detalles del producto: ${product.name}, Favorito: ${product.isFavorite}")

            binding.tvProductTitle.text = product.name
            binding.tvProductPrice.text = getString(R.string.format_price, product.price)
            binding.tvProductCategory.text = product.category
            binding.tvProductDescription.text = product.description

            updateStockDisplay(product.stock)
            updateFavoriteButton(product.isFavorite)
            updateRatingDisplay(product.rating)

            binding.ivProductDetail.load(product.image) {
                crossfade(true)
                placeholder(R.drawable.product_placeholder)
                error(R.drawable.product_placeholder)
            }
        } ?: run {
            Log.e(TAG, "Error: No se recibió producto del Intent")
            Toast.makeText(this, getString(R.string.error_load_product), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        Log.d(TAG, "Actualizando botón favorito a: $isFavorite")
        binding.btnFavoriteDetail.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )
    }

    private fun updateRatingDisplay(rating: Float) {
        val ratingText = String.format("%.1f ⭐ (%d reviews)", rating, currentProduct?.reviewCount ?: 0)
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        val ratingInt = rating.toInt().coerceIn(1, 5)

        stars.forEachIndexed { index, star ->
            star.setTextColor(
                ContextCompat.getColor(this,
                    if (index < ratingInt) R.color.warning_orange else R.color.gray_light
                )
            )
        }

        binding.tvProductRating.text = ratingText
    }

    private fun updateStockDisplay(stock: Int) {
        val cartItem = currentProduct?.let { CartManager.getCartItem(it.id) }
        val inCart = cartItem?.quantity ?: 0
        val effectiveStock = stock - inCart

        if (effectiveStock > 0) {
            binding.tvProductStock.text = getString(R.string.stock_label, stock)
            if (inCart > 0) binding.tvProductStock.append(" (${inCart} en carrito)")
            binding.tvProductStock.setTextColor(ContextCompat.getColor(this, R.color.success_green))
            binding.btnAddToCartDetail.isEnabled = true
            binding.btnAddToCartDetail.text = getString(R.string.add_to_cart_button)
        } else {
            binding.tvProductStock.text = "Sin stock disponible"
            binding.tvProductStock.setTextColor(ContextCompat.getColor(this, R.color.error_red))
            binding.btnAddToCartDetail.isEnabled = false
            binding.btnAddToCartDetail.text = "SIN STOCK"
        }
    }

    private fun addToCart(product: Product) {
        val cartItem = CartManager.getCartItem(product.id)
        val alreadyInCart = cartItem?.quantity ?: 0
        val availableStock = product.stock - alreadyInCart

        if (availableStock > 0) {
            if (viewModel.addToCart(product, 1)) {
                Toast.makeText(this, getString(R.string.add_to_cart_success, product.name), Toast.LENGTH_SHORT).show()
                updateStockDisplay(product.stock)
            } else {
                Toast.makeText(this, "No hay stock suficiente", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay stock disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumida")
        viewModel.loadProductsFromLocal()
    }
}