package com.example.instagramclone.data.remote

import com.example.instagramclone.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val postApi: PostApi = retrofit.create(PostApi::class.java)
}
