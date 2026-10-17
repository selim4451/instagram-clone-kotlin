package com.example.instagramclone.data.remote.dto

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

@Serializable
data class ErrorResponse(val error: String)
