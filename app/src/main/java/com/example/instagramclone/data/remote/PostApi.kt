package com.example.instagramclone.data.remote

import com.example.instagramclone.data.remote.dto.CommentResponse
import com.example.instagramclone.data.remote.dto.CreateCommentRequest
import com.example.instagramclone.data.remote.dto.LikeResponse
import com.example.instagramclone.data.remote.dto.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PostApi {

    @GET("posts")
    suspend fun feed(): List<PostResponse>

    @Multipart
    @POST("posts")
    suspend fun create(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
    ): PostResponse

    @POST("posts/{id}/like")
    suspend fun like(@Path("id") postId: Long): LikeResponse

    @DELETE("posts/{id}/like")
    suspend fun unlike(@Path("id") postId: Long): LikeResponse

    @GET("posts/{id}/comments")
    suspend fun comments(@Path("id") postId: Long): List<CommentResponse>

    @POST("posts/{id}/comments")
    suspend fun addComment(@Path("id") postId: Long, @Body body: CreateCommentRequest): CommentResponse

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Long)
}
