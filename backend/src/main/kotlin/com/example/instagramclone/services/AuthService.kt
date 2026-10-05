package com.example.instagramclone.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.instagramclone.UnauthorizedException
import com.example.instagramclone.ValidationException
import com.example.instagramclone.dto.AuthResponse
import com.example.instagramclone.dto.LoginRequest
import com.example.instagramclone.dto.RegisterRequest
import com.example.instagramclone.dto.UserResponse
import com.example.instagramclone.dto.toResponse
import com.example.instagramclone.repositories.UserRepository

class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
) {

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
        return userRepository.create(username, email, passwordHash).toResponse()
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)

        val hash = user?.passwordHash ?: DUMMY_HASH
        val passwordMatches = request.password.toByteArray().size <= 72 &&
            BCrypt.verifyer().verify(request.password.toCharArray(), hash).verified

        if (user == null || !passwordMatches) {
            throw UnauthorizedException("E-posta veya şifre hatalı")
        }

        return AuthResponse(token = tokenService.generate(user), user = user.toResponse())
    }

    suspend fun getUser(id: Long): UserResponse =
        userRepository.findById(id)?.toResponse()
            ?: throw UnauthorizedException("Oturum geçersiz")

    private companion object {
        const val BCRYPT_COST = 12
        val USERNAME_REGEX = Regex("^[a-z0-9._]{3,30}$")
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val DUMMY_HASH: String = BCrypt.withDefaults().hashToString(BCRYPT_COST, "dummy-password".toCharArray())
    }
}
