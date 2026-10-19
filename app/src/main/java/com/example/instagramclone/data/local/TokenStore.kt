package com.example.instagramclone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object TokenStore : TokenStorage {

    private val TOKEN_KEY = stringPreferencesKey("token")

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    override suspend fun getToken(): String? =
        appContext.dataStore.data.map { it[TOKEN_KEY] }.first()

    override suspend fun save(token: String) {
        appContext.dataStore.edit { it[TOKEN_KEY] = token }
    }

    override suspend fun clear() {
        appContext.dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
