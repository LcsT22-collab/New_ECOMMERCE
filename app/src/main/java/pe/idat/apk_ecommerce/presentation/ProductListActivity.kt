package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.databinding.ActivityProductListBinding
import pe.idat.apk_ecommerce.presentation.adapter.ProductAdapter
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory
import pe.idat.apk_ecommerce.util.CartManager

class ProductListActivity : BaseActivity() {
    private lateinit var binding: ActivityProductListBinding
    private lateinit var viewModel: AppViewModel
    private lateinit var adapter: ProductAdapter

    // Agregar TAG para logs
    private companion object {
        const val TAG = "ProductListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "ProductListActivity creado")

        // Crea el ViewModel
        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(AppRepository(applicationContext))
        )[AppViewModel::class.java]

        setupAdapter()
        setupListeners()
        setupObservers()

        viewModel.loadProductsFromLocal()
        updateCartCounter()

        checkPurchaseResult()
    }

    private fun checkPurchaseResult() {
        if (intent.getBooleanExtra("purchase_completed", false)) {
            Log.d(TAG, "Compra completada recibida del CheckoutActivity")
            Toast.makeText(this, "¡Compra realizada exitosamente!", Toast.LENGTH_LONG).show()
            viewModel.loadProductsFromLocal()
            intent.removeExtra("purchase_completed")
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            Log.d(TAG, "Botón retroceso presionado")
            onBackPressed()
        }

        binding.btnCart.setOnClickListener {
            Log.d(TAG, "Botón carrito presionado")
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupAdapter() {
        Log.d(TAG, "Configurando adaptador de productos")
        adapter = ProductAdapter(
            onItemClick = { product ->
                Log.d(TAG, "Producto clickeado: ${product.name}")
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("product", product)
                })
            },
            onAddToCart = { product, quantity ->
                Log.d(TAG, "Agregando al carrito: ${product.name}, cantidad: $quantity")
                if (viewModel.addToCart(product, quantity)) {
                    showToast("${product.name} agregado al carrito")
                    updateCartCounter()
                } else {
                    showToast("Sin stock suficiente")
                }
            },
            onToggleFavorite = { product ->
                Log.d(TAG, "Alternando favorito para: ${product.name} (ID: ${product.id})")
                // CORRECCIÓN: Pasar solo el ID del producto, no el objeto completo
                viewModel.toggleFavorite(product.id)
                showToast(if (product.isFavorite) "Removido de favoritos" else "Agregado a favoritos")
            }
        )

        binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewProducts.adapter = adapter
        Log.d(TAG, "Adaptador configurado")
    }

    private fun setupObservers() {
        viewModel.products.observe(this) { products ->
            Log.d(TAG, "Observador de productos: recibidos ${products.size} productos")
            adapter.submitList(products)
            binding.tvNoProducts.visibility = if (products.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            updateCartCounter()

            // Log adicional para ver los productos
            if (products.isNotEmpty()) {
                Log.d(TAG, "Primeros 3 productos: ${products.take(3).map { it.name }}")
            }
        }

        viewModel.cartItems.observe(this) {
            Log.d(TAG, "Observador de carrito: actualizando contador")
            updateCartCounter()
        }
    }

    private fun updateCartCounter() {
        val total = CartManager.itemCount
        Log.d(TAG, "Actualizando contador de carrito: $total items")
        binding.tvCartCount.text = total.toString()
        binding.tvCartCount.visibility = if (total > 0) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumida")
        updateCartCounter()
    }
}