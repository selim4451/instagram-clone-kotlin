package com.example.instagramclone.data.repository

import com.example.instagramclone.data.local.TokenStore
import com.example.instagramclone.data.remote.ApiClient
import com.example.instagramclone.data.remote.AuthApi
import com.example.instagramclone.data.remote.dto.AuthResponse
import com.example.instagramclone.data.remote.dto.LoginRequest
import com.example.instagramclone.data.remote.dto.RegisterRequest
import com.example.instagramclone.data.remote.dto.UserResponse
import com.example.instagramclone.util.Resource
import com.example.instagramclone.util.safeApiCall

class AuthRepository(private val api: AuthApi = ApiClient.authApi) {

    suspend fun register(username: String, email: String, password: String): Resource<UserResponse> =
        safeApiCall { api.register(RegisterRequest(username, email, password)) }

    suspend fun login(email: String, password: String): Resource<AuthResponse> =
        safeApiCall {
            api.login(LoginRequest(email, password)).also { TokenStore.save(it.token) }
        }

    suspend fun me(): Resource<UserResponse> = safeApiCall { api.me() }

    suspend fun isLoggedIn(): Boolean = TokenStore.getToken() != null

    suspend fun logout() = TokenStore.clear()
}
