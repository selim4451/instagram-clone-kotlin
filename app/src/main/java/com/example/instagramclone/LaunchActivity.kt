package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.instagramclone.data.repository.AuthRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch


class LaunchActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val destination = if (!authRepository.isLoggedIn()) {
                LoginActivity::class.java
            } else {
                val result = authRepository.me()
                if (result is Resource.Error && result.httpCode == 401) {
                    authRepository.logout()
                    LoginActivity::class.java
                } else {
                    MainActivity::class.java
                }
            }
            startActivity(Intent(this@LaunchActivity, destination))
            finish()
        }
    }
}
