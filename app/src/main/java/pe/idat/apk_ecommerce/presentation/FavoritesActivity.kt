package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityFavoritesBinding
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.presentation.adapter.ProductAdapter
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory

class FavoritesActivity : BaseActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var viewModel: AppViewModel
    private lateinit var productAdapter: ProductAdapter

    private companion object {
        const val TAG = "FavoritesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "FavoritesActivity creado")

        viewModel = ViewModelProvider(this, AppViewModelFactory(AppRepository(applicationContext)))[AppViewModel::class.java]

        setupAdapter()
        setupObservers()
        setupListeners()
    }

    private fun setupAdapter() {
        Log.d(TAG, "Configurando adaptador de favoritos")
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                Log.d(TAG, "Producto favorito clickeado: ${product.name}")
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("product", product)
                })
            },
            onAddToCart = { product, quantity ->
                Log.d(TAG, "Agregando favorito al carrito: ${product.name}, cantidad: $quantity")
                if (viewModel.addToCart(product, quantity)) {
                    Toast.makeText(
                        this,
                        getString(R.string.add_to_cart_success, product.name),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "No hay stock suficiente", Toast.LENGTH_SHORT).show()
                }
            },
            onToggleFavorite = { product ->
                Log.d(TAG, "Removiendo de favoritos: ${product.name} (ID: ${product.id})")
                // CORRECCIÓN: Pasar solo el ID del producto
                viewModel.toggleFavorite(product.id)
                Toast.makeText(
                    this,
                    getString(R.string.removed_from_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.recyclerViewFavorites.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewFavorites.adapter = productAdapter
        Log.d(TAG, "Adaptador de favoritos configurado")
    }

    private fun setupObservers() {
        viewModel.favoriteProducts.observe(this) { favoriteProducts ->
            Log.d(TAG, "Observador de favoritos: recibidos ${favoriteProducts.size} productos")
            productAdapter.submitList(favoriteProducts)
            val isEmpty = favoriteProducts.isEmpty()
            binding.tvEmptyFavorites.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
            binding.recyclerViewFavorites.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnBackFavorites.setOnClickListener {
            Log.d(TAG, "Botón retroceso presionado")
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "FavoritesActivity resumida")
        viewModel.loadFavoriteProducts()
    }
}