package com.example.instagramclone

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instagramclone.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

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
                Toast.makeText(this, R.string.register_form_ok, Toast.LENGTH_SHORT).show()
            }
        }

        // Yeni Intent açmıyoruz: bu ekranı kapatınca alttaki Login zaten görünür.
        binding.loginLink.setOnClickListener {
            finish()
        }
    }

    // Form geçerliyse true döner; değilse ilgili alanın altına hata yazar.
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
