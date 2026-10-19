package com.example.instagramclone.data.local

interface TokenStorage {
    suspend fun getToken(): String?
    suspend fun save(token: String)
    suspend fun clear()
}
