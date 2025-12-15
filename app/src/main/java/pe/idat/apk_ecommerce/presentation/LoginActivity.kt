package pe.idat.apk_ecommerce.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ActivityLoginBinding
import pe.idat.apk_ecommerce.data.AppRepository
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModel
import pe.idat.apk_ecommerce.presentation.viewmodel.AppViewModelFactory


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, AppViewModelFactory(AppRepository(applicationContext)))[AppViewModel::class.java]

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInputs(email, password)) {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = getString(R.string.loading_login)

                viewModel.login(email, password) { result ->
                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(R.string.login_button)

                        result.onSuccess {
                            goToMainActivity()
                        }.onFailure {
                            Toast.makeText(
                                this,
                                it.message ?: getString(R.string.error_login),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}