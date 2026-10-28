package com.example.instagramclone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.databinding.ActivityCommentsBinding
import com.example.instagramclone.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlin.math.max

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding

    private val viewModel: CommentsViewModel by viewModels()

    private val adapter = CommentAdapter(onLongClick = { confirmDelete(it.id) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, max(systemBars.bottom, ime.bottom))
            insets
        }

        binding.commentList.layoutManager = LinearLayoutManager(this)
        binding.commentList.adapter = adapter
        binding.backButton.setOnClickListener { finish() }

        binding.commentInput.doAfterTextChanged { updateSendButton() }
        binding.sendButton.setOnClickListener { viewModel.send(binding.commentInput.text.toString()) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.comments.collect { state ->
                        binding.progressIndicator.isVisible = state is Resource.Loading
                        val comments = (state as? Resource.Success)?.data
                        adapter.submitList(comments)
                        binding.emptyText.isVisible = comments?.isEmpty() == true
                        if (state is Resource.Error) showError(state)
                    }
                }
                launch {
                    viewModel.isSending.collect { updateSendButton() }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            CommentsEvent.Sent -> {
                                binding.commentInput.text = null
                                binding.commentList.post {
                                    binding.commentList.smoothScrollToPosition(maxOf(adapter.itemCount - 1, 0))
                                }
                            }
                            is CommentsEvent.Failed -> showError(event.error)
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        if (viewModel.changed) setResult(Activity.RESULT_OK)
        super.finish()
    }

    private fun updateSendButton() {
        binding.sendButton.isEnabled =
            !binding.commentInput.text.isNullOrBlank() && !viewModel.isSending.value
    }

    private fun confirmDelete(commentId: Long) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.comments_delete_title)
            .setPositiveButton(R.string.comments_delete) { _, _ -> viewModel.delete(commentId) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showError(error: Resource.Error) {
        val message = error.message ?: getString(
            if (error.isNetworkError) R.string.error_network else R.string.error_unexpected
        )
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_POST_ID = "postId"

        fun intent(context: Context, postId: Long): Intent =
            Intent(context, CommentsActivity::class.java).putExtra(EXTRA_POST_ID, postId)
    }
}
