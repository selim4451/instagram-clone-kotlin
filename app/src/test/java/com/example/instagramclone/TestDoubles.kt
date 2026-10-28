package com.example.instagramclone

import com.example.instagramclone.data.local.TokenStorage
import com.example.instagramclone.data.remote.AuthApi
import com.example.instagramclone.data.remote.PostApi
import com.example.instagramclone.data.remote.dto.AuthResponse
import com.example.instagramclone.data.remote.dto.AuthorResponse
import com.example.instagramclone.data.remote.dto.CommentResponse
import com.example.instagramclone.data.remote.dto.CreateCommentRequest
import com.example.instagramclone.data.remote.dto.LikeResponse
import com.example.instagramclone.data.remote.dto.LoginRequest
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.data.remote.dto.RegisterRequest
import com.example.instagramclone.data.remote.dto.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeTokenStorage(var token: String? = null) : TokenStorage {
    override suspend fun getToken() = token
    override suspend fun save(token: String) {
        this.token = token
    }
    override suspend fun clear() {
        token = null
    }
}

class FakeAuthApi : AuthApi {
    var onRegister: suspend (RegisterRequest) -> UserResponse = { UserResponse(1, it.username, it.email) }
    var onLogin: suspend (LoginRequest) -> AuthResponse = { AuthResponse("token-123", UserResponse(1, "selim", it.email)) }
    var onMe: suspend () -> UserResponse = { UserResponse(1, "selim", "selim@example.com") }

    override suspend fun register(body: RegisterRequest) = onRegister(body)
    override suspend fun login(body: LoginRequest) = onLogin(body)
    override suspend fun me() = onMe()
}

class FakePostApi : PostApi {
    var onFeed: suspend () -> List<PostResponse> = { emptyList() }
    var onLike: suspend (Long, Boolean) -> LikeResponse = { _, liked -> LikeResponse(if (liked) 1 else 0, liked) }
    var onComments: suspend (Long) -> List<CommentResponse> = { emptyList() }
    var onAddComment: suspend (Long, String) -> CommentResponse = { _, text -> comment(100, text) }
    var onDeleteComment: suspend (Long) -> Unit = {}
    val likeCalls = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun feed() = onFeed()
    override suspend fun create(image: MultipartBody.Part, caption: RequestBody): PostResponse =
        error("not used in these tests")
    override suspend fun like(postId: Long): LikeResponse {
        likeCalls += postId to true
        return onLike(postId, true)
    }
    override suspend fun unlike(postId: Long): LikeResponse {
        likeCalls += postId to false
        return onLike(postId, false)
    }
    override suspend fun comments(postId: Long) = onComments(postId)
    override suspend fun addComment(postId: Long, body: CreateCommentRequest) = onAddComment(postId, body.text)
    override suspend fun deleteComment(commentId: Long) = onDeleteComment(commentId)
}

fun httpError(code: Int, message: String): HttpException =
    HttpException(
        Response.error<Any>(code, """{"error":"$message"}""".toResponseBody("application/json".toMediaType()))
    )

fun post(id: Long, caption: String, likeCount: Int = 0, likedByMe: Boolean = false) = PostResponse(
    id = id,
    imageUrl = "/uploads/$id.jpg",
    caption = caption,
    createdAt = 0,
    author = AuthorResponse(1, "selim"),
    likeCount = likeCount,
    likedByMe = likedByMe,
)

fun comment(id: Long, text: String, canDelete: Boolean = true) = CommentResponse(
    id = id,
    text = text,
    createdAt = 0,
    author = AuthorResponse(1, "selim"),
    canDelete = canDelete,
)
