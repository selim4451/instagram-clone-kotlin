package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instagramclone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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
            // Geçici: şifre henüz kontrol edilmiyor, backend gelince gerçek girişe çevrilecek.
            if (validateForm()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Form geçerliyse true döner; değilse ilgili alanın altına hata yazar.
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
