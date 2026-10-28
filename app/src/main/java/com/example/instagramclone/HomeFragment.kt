package com.example.instagramclone

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val openComments = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) viewModel.refresh()
    }

    private val adapter = PostAdapter(
        onLikeClick = { viewModel.toggleLike(it.id) },
        onDoubleTap = { viewModel.likeFromDoubleTap(it.id) },
        onCommentsClick = { openComments.launch(CommentsActivity.intent(requireContext(), it.id)) },
    )

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
        (binding.postList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
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
