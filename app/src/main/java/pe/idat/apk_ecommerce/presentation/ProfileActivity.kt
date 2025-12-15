package pe.idat.apk_ecommerce.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityProfileBinding


class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        binding.btnBackProfile.setOnClickListener { onBackPressed() }
        binding.btnChangePassword.setOnClickListener {
            // Puedes agregar funcionalidad aquí si lo deseas
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser

        if (user != null) {
            binding.tvProfileEmail.text = user.email ?: getString(R.string.not_specified)
            binding.tvProfileUid.text = user.uid
            binding.tvProfileName.text = user.displayName ?: getString(R.string.default_user)
            binding.tvProfilePassword.text = getString(R.string.password_display)
        }
    }
}