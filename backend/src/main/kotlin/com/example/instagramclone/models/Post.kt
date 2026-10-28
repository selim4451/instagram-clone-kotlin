package com.example.instagramclone.models

import java.time.Instant

data class Post(
    val id: Long,
    val authorId: Long,
    val authorUsername: String,
    val imageName: String,
    val caption: String,
    val createdAt: Instant,
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,
    val commentCount: Int = 0,
)
