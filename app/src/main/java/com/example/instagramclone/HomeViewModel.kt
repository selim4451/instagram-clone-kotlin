package com.example.instagramclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PostRepository = PostRepository(),
) : ViewModel() {

    private val _feed = MutableStateFlow<Resource<List<PostResponse>>>(Resource.Loading)
    val feed: StateFlow<Resource<List<PostResponse>>> = _feed.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<Resource.Error?>(null)
    val error: StateFlow<Resource.Error?> = _error.asStateFlow()

    private val pendingLikes = mutableSetOf<Long>()

    init {
        refresh()
    }

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.feed()) {
                is Resource.Success -> _feed.value = result
                is Resource.Error -> _error.value = result
                Resource.Loading -> Unit
            }
            _isRefreshing.value = false
        }
    }

    fun toggleLike(postId: Long) {
        val original = findPost(postId) ?: return
        if (!pendingLikes.add(postId)) return

        val liked = !original.likedByMe
        updatePost(postId) {
            it.copy(likedByMe = liked, likeCount = it.likeCount + if (liked) 1 else -1)
        }

        viewModelScope.launch {
            when (val result = repository.setLiked(postId, liked)) {
                is Resource.Success -> updatePost(postId) {
                    it.copy(likedByMe = result.data.likedByMe, likeCount = result.data.likeCount)
                }
                is Resource.Error -> {
                    updatePost(postId) {
                        it.copy(likedByMe = original.likedByMe, likeCount = original.likeCount)
                    }
                    _error.value = result
                }
                Resource.Loading -> Unit
            }
            pendingLikes.remove(postId)
        }
    }

    fun likeFromDoubleTap(postId: Long) {
        if (findPost(postId)?.likedByMe == false) toggleLike(postId)
    }

    fun errorShown() {
        _error.value = null
    }

    private fun findPost(postId: Long): PostResponse? =
        (_feed.value as? Resource.Success)?.data?.firstOrNull { it.id == postId }

    private fun updatePost(postId: Long, transform: (PostResponse) -> PostResponse) {
        val posts = (_feed.value as? Resource.Success)?.data ?: return
        _feed.value = Resource.Success(posts.map { if (it.id == postId) transform(it) else it })
    }
}
