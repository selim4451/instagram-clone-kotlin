package com.example.instagramclone.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.instagramclone.ValidationException
import com.example.instagramclone.models.RegisterRequest
import com.example.instagramclone.models.UserResponse
import com.example.instagramclone.repositories.UserRepository

class AuthService(private val userRepository: UserRepository) {

    suspend fun register(request: RegisterRequest): UserResponse {
        val username = request.username.trim().lowercase()
        val email = request.email.trim().lowercase()
        val password = request.password

        if (!USERNAME_REGEX.matches(username)) {
            throw ValidationException("Kullanıcı adı 3-30 karakter olmalı; sadece küçük harf, rakam, nokta ve alt çizgi içerebilir")
        }
        if (!EMAIL_REGEX.matches(email)) {
            throw ValidationException("Geçerli bir e-posta gir")
        }
        if (password.length < 6 || password.toByteArray().size > 72) {
            throw ValidationException("Şifre en az 6, en fazla 72 karakter olmalı")
        }

        val passwordHash = BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
        return userRepository.create(username, email, passwordHash)
    }

    private companion object {
        const val BCRYPT_COST = 12
        val USERNAME_REGEX = Regex("^[a-z0-9._]{3,30}$")
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
