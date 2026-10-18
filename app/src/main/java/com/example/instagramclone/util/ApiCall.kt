package com.example.instagramclone.util

import android.util.Log
import com.example.instagramclone.data.remote.ApiClient
import com.example.instagramclone.data.remote.dto.ErrorResponse
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> =
    try {
        Resource.Success(call())
    } catch (e: HttpException) {
        Resource.Error(message = e.errorMessage(), httpCode = e.code())
    } catch (e: IOException) {
        Log.w("ApiCall", "Network error", e)
        Resource.Error(isNetworkError = true)
    } catch (e: SerializationException) {
        Log.w("ApiCall", "Unexpected response", e)
        Resource.Error()
    }

private fun HttpException.errorMessage(): String? =
    response()?.errorBody()?.string()?.let { body ->
        runCatching { ApiClient.json.decodeFromString<ErrorResponse>(body).error }.getOrNull()
    }
