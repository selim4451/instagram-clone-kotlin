package com.example.instagramclone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val adapter = PostAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.postList.layoutManager = LinearLayoutManager(requireContext())
        binding.postList.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.feed.collect { state ->
                        val posts = (state as? Resource.Success)?.data
                        adapter.submitList(posts)
                        binding.emptyText.isVisible = posts?.isEmpty() == true
                    }
                }
                launch {
                    viewModel.isRefreshing.collect { binding.swipeRefresh.isRefreshing = it }
                }
                launch {
                    viewModel.error.collect { error ->
                        if (error != null) {
                            val message = error.message ?: getString(
                                if (error.isNetworkError) R.string.error_network else R.string.error_unexpected
                            )
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                            viewModel.errorShown()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.postList.adapter = null
        _binding = null
    }
}
