package com.example.instagramclone.services

import com.example.instagramclone.ForbiddenException
import com.example.instagramclone.NotFoundException
import com.example.instagramclone.ValidationException
import com.example.instagramclone.dto.CommentResponse
import com.example.instagramclone.dto.LikeResponse
import com.example.instagramclone.dto.PostResponse
import com.example.instagramclone.dto.toResponse
import com.example.instagramclone.repositories.CommentRepository
import com.example.instagramclone.repositories.LikeRepository
import com.example.instagramclone.repositories.PostRepository
import com.example.instagramclone.storage.ImageStorage

class PostService(
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val imageStorage: ImageStorage,
) {

    suspend fun create(userId: Long, imageName: String?, caption: String): PostResponse {
        val trimmedCaption = caption.trim()
        if (imageName == null) {
            throw ValidationException("Bir fotoğraf seçmelisin")
        }
        if (trimmedCaption.length > MAX_TEXT_LENGTH) {
            imageStorage.delete(imageName)
            throw ValidationException("Açıklama en fazla $MAX_TEXT_LENGTH karakter olabilir")
        }
        return postRepository.create(userId, imageName, trimmedCaption).toResponse()
    }

    suspend fun feed(viewerId: Long): List<PostResponse> =
        postRepository.findLatest(viewerId, FEED_LIMIT).map { it.toResponse() }

    suspend fun like(userId: Long, postId: Long): LikeResponse {
        requirePost(postId)
        likeRepository.like(userId, postId)
        return LikeResponse(likeCount = likeRepository.count(postId), likedByMe = true)
    }

    suspend fun unlike(userId: Long, postId: Long): LikeResponse {
        requirePost(postId)
        likeRepository.unlike(userId, postId)
        return LikeResponse(likeCount = likeRepository.count(postId), likedByMe = false)
    }

    suspend fun comments(viewerId: Long, postId: Long): List<CommentResponse> {
        requirePost(postId)
        return commentRepository.findByPost(postId).map { it.toResponse(viewerId) }
    }

    suspend fun addComment(userId: Long, postId: Long, text: String): CommentResponse {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            throw ValidationException("Yorum boş olamaz")
        }
        if (trimmed.length > MAX_TEXT_LENGTH) {
            throw ValidationException("Yorum en fazla $MAX_TEXT_LENGTH karakter olabilir")
        }
        requirePost(postId)
        return commentRepository.create(postId, userId, trimmed).toResponse(userId)
    }

    suspend fun deleteComment(userId: Long, commentId: Long) {
        val comment = commentRepository.findById(commentId) ?: throw NotFoundException("Yorum bulunamadı")
        if (userId != comment.authorId && userId != comment.postAuthorId) {
            throw ForbiddenException("Bu yorumu silemezsin")
        }
        commentRepository.delete(commentId)
    }

    private suspend fun requirePost(postId: Long) {
        if (!postRepository.exists(postId)) throw NotFoundException("Gönderi bulunamadı")
    }

    private companion object {
        const val MAX_TEXT_LENGTH = 2200
        const val FEED_LIMIT = 50
    }
}
