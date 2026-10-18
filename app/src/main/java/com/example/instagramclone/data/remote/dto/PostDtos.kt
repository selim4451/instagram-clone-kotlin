package com.example.instagramclone.data.remote.dto

import com.example.instagramclone.BuildConfig
import kotlinx.serialization.Serializable

@Serializable
data class AuthorResponse(
    val id: Long,
    val username: String,
)

@Serializable
data class PostResponse(
    val id: Long,
    val imageUrl: String,
    val caption: String,
    val createdAt: Long,
    val author: AuthorResponse,
) {
    val fullImageUrl: String get() = BuildConfig.BASE_URL.trimEnd('/') + imageUrl
}
