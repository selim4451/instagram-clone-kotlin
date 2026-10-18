package com.example.instagramclone.services

import com.example.instagramclone.ValidationException
import com.example.instagramclone.dto.PostResponse
import com.example.instagramclone.dto.toResponse
import com.example.instagramclone.repositories.PostRepository
import com.example.instagramclone.storage.ImageStorage

class PostService(
    private val postRepository: PostRepository,
    private val imageStorage: ImageStorage,
) {

    suspend fun create(userId: Long, imageName: String?, caption: String): PostResponse {
        val trimmedCaption = caption.trim()
        if (imageName == null) {
            throw ValidationException("Bir fotoğraf seçmelisin")
        }
        if (trimmedCaption.length > MAX_CAPTION_LENGTH) {
            imageStorage.delete(imageName)
            throw ValidationException("Açıklama en fazla $MAX_CAPTION_LENGTH karakter olabilir")
        }
        return postRepository.create(userId, imageName, trimmedCaption).toResponse()
    }

    suspend fun feed(): List<PostResponse> =
        postRepository.findLatest(FEED_LIMIT).map { it.toResponse() }

    private companion object {
        const val MAX_CAPTION_LENGTH = 2200
        const val FEED_LIMIT = 50
    }
}
