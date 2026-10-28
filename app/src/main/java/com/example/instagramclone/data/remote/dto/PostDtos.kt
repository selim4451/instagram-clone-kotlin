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
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,
    val commentCount: Int = 0,
) {
    val fullImageUrl: String get() = BuildConfig.BASE_URL.trimEnd('/') + imageUrl
}

@Serializable
data class LikeResponse(
    val likeCount: Int,
    val likedByMe: Boolean,
)

@Serializable
data class CreateCommentRequest(val text: String)

@Serializable
data class CommentResponse(
    val id: Long,
    val text: String,
    val createdAt: Long,
    val author: AuthorResponse,
    val canDelete: Boolean,
)
