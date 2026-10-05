package com.example.instagramclone.models

import java.time.Instant

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,
)
