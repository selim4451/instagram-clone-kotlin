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

    fun errorShown() {
        _error.value = null
    }
}
