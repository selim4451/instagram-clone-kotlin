package com.example.instagramclone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.data.remote.dto.CommentResponse
import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CommentsViewModel @JvmOverloads constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PostRepository = PostRepository(),
) : ViewModel() {

    private val postId: Long = checkNotNull(savedStateHandle[CommentsActivity.EXTRA_POST_ID])

    private val _comments = MutableStateFlow<Resource<List<CommentResponse>>>(Resource.Loading)
    val comments: StateFlow<Resource<List<CommentResponse>>> = _comments.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _events = Channel<CommentsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    var changed = false
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _comments.value = Resource.Loading
            _comments.value = repository.comments(postId)
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            when (val result = repository.addComment(postId, trimmed)) {
                is Resource.Success -> {
                    updateComments { it + result.data }
                    changed = true
                    _events.send(CommentsEvent.Sent)
                }
                is Resource.Error -> _events.send(CommentsEvent.Failed(result))
                Resource.Loading -> Unit
            }
            _isSending.value = false
        }
    }

    fun delete(commentId: Long) {
        viewModelScope.launch {
            when (val result = repository.deleteComment(commentId)) {
                is Resource.Success -> {
                    updateComments { comments -> comments.filterNot { it.id == commentId } }
                    changed = true
                }
                is Resource.Error -> _events.send(CommentsEvent.Failed(result))
                Resource.Loading -> Unit
            }
        }
    }

    private fun updateComments(transform: (List<CommentResponse>) -> List<CommentResponse>) {
        val current = (_comments.value as? Resource.Success)?.data ?: return
        _comments.value = Resource.Success(transform(current))
    }
}

sealed interface CommentsEvent {
    data object Sent : CommentsEvent
    data class Failed(val error: Resource.Error) : CommentsEvent
}
