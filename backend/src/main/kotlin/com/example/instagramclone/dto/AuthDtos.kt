package com.example.instagramclone.dto

import com.example.instagramclone.models.User
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)

fun User.toResponse() = UserResponse(id = id, username = username, email = email)
