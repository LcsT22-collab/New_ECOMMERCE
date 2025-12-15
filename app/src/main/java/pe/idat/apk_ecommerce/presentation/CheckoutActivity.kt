package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityCheckoutBinding
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory
import pe.idat.apk_ecommerce.util.CartManager

class CheckoutActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, AppViewModelFactory(AppRepository(applicationContext)))[AppViewModel::class.java]

        setupClickListeners()
        loadOrderSummary()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.purchaseResult.observe(this) { result ->
            result?.let { (success, message) ->
                if (success) {
                    Toast.makeText(this, message ?: "Compra realizada", Toast.LENGTH_LONG).show()
                    viewModel.loadProductsFromLocal()
                    val intent = Intent(this, ProductListActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("purchase_completed", true)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Error en la compra", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBackCheckout.setOnClickListener { onBackPressed() }
        binding.btnConfirmPurchase.setOnClickListener { confirmPurchase() }
    }

    private fun confirmPurchase() {
        if (CartManager.items.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_cart_error), Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.processPurchase()
    }

    private fun loadOrderSummary() {
        binding.tvCheckoutTotal.text = getString(R.string.format_price, CartManager.total)
        binding.tvItemCount.text = CartManager.itemCount.toString()

        val summary = StringBuilder()
        CartManager.items.forEachIndexed { index, product ->
            val productTotal = product.price * product.quantity
            summary.append(
                getString(
                    R.string.order_summary_format,
                    index + 1,
                    product.name,
                    product.quantity,
                    getString(R.string.format_price, product.price),
                    getString(R.string.format_price, productTotal)
                )
            )
        }

        if (summary.isNotEmpty()) {
            binding.tvOrderSummary.text = summary.toString()
        }
    }
}