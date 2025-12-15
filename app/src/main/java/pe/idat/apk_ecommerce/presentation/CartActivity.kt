package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pe.idat.apk_ecommerce.databinding.ActivityCartBinding
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.presentation.adapter.CartAdapter
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory
import pe.idat.apk_ecommerce.util.CartManager


class CartActivity : BaseActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: AppViewModel
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(AppRepository(applicationContext))
        )[AppViewModel::class.java]

        setupAdapter()
        setupObservers()
        setupListeners()

        updateCartDisplay()
    }

    private fun setupAdapter() {
        adapter = CartAdapter(
            cartItems = CartManager.items,
            onRemoveFromCart = { product ->
                viewModel.removeFromCart(product)
                showToast("${product.name} removido")
                updateCartDisplay()
            },
            onUpdateQuantity = { product, newQuantity ->
                if (viewModel.updateCartItemQuantity(product.id, newQuantity)) {
                    showToast("Cantidad actualizada")
                    updateCartDisplay()
                } else {
                    showToast("Sin stock suficiente")
                }
            }
        )

        binding.recyclerViewCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCart.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBackCart.setOnClickListener {
            onBackPressed()
        }

        binding.btnCheckout.setOnClickListener {
            if (CartManager.items.isNotEmpty()) {
                startActivity(Intent(this, CheckoutActivity::class.java))
            } else {
                showToast("Carrito vacío")
            }
        }
    }

    private fun setupObservers() {
        viewModel.cartItems.observe(this) { items ->
            adapter.updateCartItems(items)
            updateCartDisplay()
        }
    }

    private fun updateCartDisplay() {
        binding.tvTotal.text = String.format("S/. %.2f", CartManager.total)

        val isEmpty = CartManager.items.isEmpty()
        binding.tvEmptyCart.visibility = if (isEmpty) VISIBLE else GONE
        binding.btnCheckout.visibility = if (isEmpty) GONE else VISIBLE

        adapter.updateCartItems(CartManager.items)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateCartDisplay()
    }
}