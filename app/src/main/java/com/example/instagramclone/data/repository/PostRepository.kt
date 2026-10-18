package com.example.instagramclone.data.repository

import com.example.instagramclone.data.remote.ApiClient
import com.example.instagramclone.data.remote.PostApi
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.util.Resource
import com.example.instagramclone.util.safeApiCall
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PostRepository(private val api: PostApi = ApiClient.postApi) {

    suspend fun feed(): Resource<List<PostResponse>> = safeApiCall { api.feed() }

    suspend fun create(jpegBytes: ByteArray, caption: String): Resource<PostResponse> =
        safeApiCall {
            val image = MultipartBody.Part.createFormData(
                name = "image",
                filename = "photo.jpg",
                body = jpegBytes.toRequestBody("image/jpeg".toMediaType()),
            )
            val captionBody = caption.toRequestBody("text/plain; charset=utf-8".toMediaType())
            api.create(image, captionBody)
        }
}
