package com.example.instagramclone.dto

import com.example.instagramclone.models.Comment
import com.example.instagramclone.models.Post
import kotlinx.serialization.Serializable

@Serializable
data class AuthorResponse(
    val id: Long,
    val username: String,
)

@Serializable
data class PostResponse(
    val id: Long,
    val imageUrl: String,
    val caption: String,
    val createdAt: Long,
    val author: AuthorResponse,
    val likeCount: Int,
    val likedByMe: Boolean,
    val commentCount: Int,
)

fun Post.toResponse() = PostResponse(
    id = id,
    imageUrl = "/uploads/$imageName",
    caption = caption,
    createdAt = createdAt.toEpochMilli(),
    author = AuthorResponse(id = authorId, username = authorUsername),
    likeCount = likeCount,
    likedByMe = likedByMe,
    commentCount = commentCount,
)

@Serializable
data class LikeResponse(
    val likeCount: Int,
    val likedByMe: Boolean,
)

@Serializable
data class CreateCommentRequest(val text: String)

@Serializable
data class CommentResponse(
    val id: Long,
    val text: String,
    val createdAt: Long,
    val author: AuthorResponse,
    val canDelete: Boolean,
)

fun Comment.toResponse(viewerId: Long) = CommentResponse(
    id = id,
    text = text,
    createdAt = createdAt.toEpochMilli(),
    author = AuthorResponse(id = authorId, username = authorUsername),
    canDelete = viewerId == authorId || viewerId == postAuthorId,
)
