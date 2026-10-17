package com.example.instagramclone.data.repository

import android.util.Log
import com.example.instagramclone.data.remote.ApiClient
import com.example.instagramclone.data.remote.AuthApi
import com.example.instagramclone.data.remote.dto.AuthResponse
import com.example.instagramclone.data.remote.dto.ErrorResponse
import com.example.instagramclone.data.remote.dto.LoginRequest
import com.example.instagramclone.data.remote.dto.RegisterRequest
import com.example.instagramclone.data.remote.dto.UserResponse
import com.example.instagramclone.util.Resource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val api: AuthApi = ApiClient.authApi) {

    suspend fun register(username: String, email: String, password: String): Resource<UserResponse> =
        safeApiCall { api.register(RegisterRequest(username, email, password)) }

    suspend fun login(email: String, password: String): Resource<AuthResponse> =
        safeApiCall { api.login(LoginRequest(email, password)) }

    private suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> =
        try {
            Resource.Success(call())
        } catch (e: HttpException) {
            Resource.Error(message = e.errorMessage())
        } catch (e: IOException) {
            Log.w(TAG, "Network error", e)
            Resource.Error(isNetworkError = true)
        } catch (e: SerializationException) {
            Resource.Error()
        }

    private fun HttpException.errorMessage(): String? =
        response()?.errorBody()?.string()?.let { body ->
            runCatching { ApiClient.json.decodeFromString<ErrorResponse>(body).error }.getOrNull()
        }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
