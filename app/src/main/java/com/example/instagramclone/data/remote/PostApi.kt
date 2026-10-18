package com.example.instagramclone.data.remote

import com.example.instagramclone.data.remote.dto.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PostApi {

    @GET("posts")
    suspend fun feed(): List<PostResponse>

    @Multipart
    @POST("posts")
    suspend fun create(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
    ): PostResponse
}
