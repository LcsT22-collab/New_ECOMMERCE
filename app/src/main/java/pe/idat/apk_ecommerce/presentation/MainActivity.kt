package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityMainBinding
import pe.idat.apk_ecommerce.util.CartManager

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthentication()
        setupUI()
        setupListeners()
        updateCartCounter()
    }

    private fun checkAuthentication() {
        val user = auth.currentUser
        if (user == null) {
            goToLogin()
        }
    }

    private fun setupUI() {
        val user = auth.currentUser
        user?.let {
            binding.tvUserEmail.text = it.email ?: getString(R.string.default_email)
            val displayName = it.displayName ?: it.email?.split("@")?.first() ?: getString(R.string.default_user)
            binding.tvWelcome.text = getString(R.string.user_greeting, displayName)
        }
    }

    private fun setupListeners() {
        binding.btnProducts.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        binding.btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            CartManager.clearCart()
            goToLogin()
        }
    }

    private fun updateCartCounter() {
        val total = CartManager.itemCount
        binding.tvCartCount.text = total.toString()
        binding.tvCartCount.visibility = if (total > 0) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null && !isFinishing) {
            goToLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartCounter()
    }
}