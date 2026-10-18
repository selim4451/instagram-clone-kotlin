package com.example.instagramclone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import com.example.instagramclone.databinding.FragmentCreateBinding
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch

class CreateFragment : Fragment() {

    private var _binding : FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.selectImage(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.previewImage.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.shareButton.setOnClickListener {
            viewModel.share(binding.captionInput.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedImage.collect { uri ->
                        binding.previewHint.isVisible = uri == null
                        if (uri == null) binding.previewImage.setImageDrawable(null) else binding.previewImage.load(uri)
                        updateShareButton()
                    }
                }
                launch {
                    viewModel.uploadState.collect { state -> render(state) }
                }
            }
        }
    }

    private fun render(state: Resource<*>?) {
        val isLoading = state is Resource.Loading
        binding.progressIndicator.isVisible = isLoading
        binding.previewImage.isEnabled = !isLoading
        binding.captionInput.isEnabled = !isLoading
        updateShareButton()

        when (state) {
            is Resource.Success -> {
                Toast.makeText(requireContext(), R.string.create_success, Toast.LENGTH_SHORT).show()
                binding.captionInput.text = null
                viewModel.reset()
                (requireActivity() as MainActivity).openHome()
            }
            is Resource.Error -> {
                val message = state.message ?: getString(
                    if (state.isNetworkError) R.string.error_network else R.string.error_unexpected
                )
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
            Resource.Loading, null -> Unit
        }
    }

    private fun updateShareButton() {
        binding.shareButton.isEnabled =
            viewModel.selectedImage.value != null && viewModel.uploadState.value !is Resource.Loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
