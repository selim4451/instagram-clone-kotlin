package com.example.instagramclone

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.ImageCompressor
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepository()

    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImage: StateFlow<Uri?> = _selectedImage.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<PostResponse>?>(null)
    val uploadState: StateFlow<Resource<PostResponse>?> = _uploadState.asStateFlow()

    fun selectImage(uri: Uri) {
        _selectedImage.value = uri
    }

    fun share(caption: String) {
        val uri = _selectedImage.value ?: return
        if (_uploadState.value is Resource.Loading) return

        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            val bytes = runCatching {
                withContext(Dispatchers.Default) {
                    ImageCompressor.compress(getApplication<Application>().contentResolver, uri)
                }
            }.getOrNull()

            _uploadState.value = if (bytes == null) {
                Resource.Error(message = getApplication<Application>().getString(R.string.create_image_error))
            } else {
                repository.create(bytes, caption.trim())
            }
        }
    }

    fun reset() {
        _selectedImage.value = null
        _uploadState.value = null
    }
}
