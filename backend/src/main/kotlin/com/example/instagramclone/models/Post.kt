package com.example.instagramclone.models

import java.time.Instant

data class Post(
    val id: Long,
    val authorId: Long,
    val authorUsername: String,
    val imageName: String,
    val caption: String,
    val createdAt: Instant,
)
