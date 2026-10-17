package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.instagramclone.data.remote.dto.AuthResponse
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            if (validateForm()) {
                viewModel.login(
                    email = binding.emailInput.text.toString().trim(),
                    password = binding.passwordInput.text.toString(),
                )
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: Resource<AuthResponse>?) {
        val isLoading = state is Resource.Loading
        binding.progressIndicator.isVisible = isLoading
        binding.loginButton.isEnabled = !isLoading

        when (state) {
            is Resource.Success -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is Resource.Error -> {
                val message = state.message ?: getString(
                    if (state.isNetworkError) R.string.error_network else R.string.error_unexpected
                )
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            Resource.Loading, null -> Unit
        }
    }

    private fun validateForm(): Boolean {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        binding.emailLayout.error = when {
            email.isEmpty() -> getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            else -> null
        }

        binding.passwordLayout.error = when {
            password.isEmpty() -> getString(R.string.error_password_empty)
            password.length < 6 -> getString(R.string.error_password_short)
            else -> null
        }

        return binding.emailLayout.error == null && binding.passwordLayout.error == null
    }
}
