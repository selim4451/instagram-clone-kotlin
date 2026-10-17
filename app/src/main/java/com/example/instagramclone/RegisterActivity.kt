package com.example.instagramclone

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
import com.example.instagramclone.data.remote.dto.UserResponse
import com.example.instagramclone.databinding.ActivityRegisterBinding
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.registerButton.setOnClickListener {
            if (validateForm()) {
                viewModel.register(
                    username = binding.userNameInput.text.toString().trim(),
                    email = binding.emailInput.text.toString().trim(),
                    password = binding.passwordInput.text.toString(),
                )
            }
        }

        binding.loginLink.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: Resource<UserResponse>?) {
        val isLoading = state is Resource.Loading
        binding.progressIndicator.isVisible = isLoading
        binding.registerButton.isEnabled = !isLoading

        when (state) {
            is Resource.Success -> {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_LONG).show()
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
        val userName = binding.userNameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        binding.userNameLayout.error = when {
            userName.isEmpty() -> getString(R.string.error_username_empty)
            userName.length < 3 -> getString(R.string.error_username_short)
            else -> null
        }

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

        binding.confirmPasswordLayout.error = when {
            confirmPassword.isEmpty() -> getString(R.string.error_confirm_password_empty)
            confirmPassword != password -> getString(R.string.error_password_mismatch)
            else -> null
        }

        return listOf(
            binding.userNameLayout,
            binding.emailLayout,
            binding.passwordLayout,
            binding.confirmPasswordLayout,
        ).all { it.error == null }
    }
}
