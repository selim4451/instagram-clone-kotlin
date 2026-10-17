package com.example.instagramclone.util

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>

    data class Error(
        val message: String? = null,
        val isNetworkError: Boolean = false,
    ) : Resource<Nothing>
}
