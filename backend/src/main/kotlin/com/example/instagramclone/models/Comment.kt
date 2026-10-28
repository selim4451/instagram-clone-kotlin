package com.example.instagramclone.models

import java.time.Instant

data class Comment(
    val id: Long,
    val postId: Long,
    val postAuthorId: Long,
    val authorId: Long,
    val authorUsername: String,
    val text: String,
    val createdAt: Instant,
)
