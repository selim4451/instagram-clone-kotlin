package com.example.instagramclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.data.remote.dto.UserResponse
import com.example.instagramclone.data.repository.AuthRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<UserResponse>?>(null)
    val state: StateFlow<Resource<UserResponse>?> = _state.asStateFlow()

    fun register(username: String, email: String, password: String) {
        if (_state.value is Resource.Loading) return

        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repository.register(username, email, password)
        }
    }
}
