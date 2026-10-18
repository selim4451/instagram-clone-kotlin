package com.example.instagramclone.dto

import com.example.instagramclone.models.Post
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
)

fun Post.toResponse() = PostResponse(
    id = id,
    imageUrl = "/uploads/$imageName",
    caption = caption,
    createdAt = createdAt.toEpochMilli(),
    author = AuthorResponse(id = authorId, username = authorUsername),
)
