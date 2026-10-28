package com.example.instagramclone

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.inSpans
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.remote.dto.CommentResponse
import com.example.instagramclone.databinding.ItemCommentBinding
import com.example.instagramclone.util.relativeTime

class CommentAdapter(
    private val onLongClick: (CommentResponse) -> Unit,
) : ListAdapter<CommentResponse, CommentAdapter.CommentViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentResponse) {
            binding.commentText.text = SpannableStringBuilder()
                .inSpans(StyleSpan(Typeface.BOLD)) { append(comment.author.username) }
                .append(" ")
                .append(comment.text)
            binding.timeText.text = relativeTime(binding.root.context, comment.createdAt)

            if (comment.canDelete) {
                binding.root.setOnLongClickListener {
                    onLongClick(comment)
                    true
                }
            } else {
                binding.root.setOnLongClickListener(null)
                binding.root.isLongClickable = false
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CommentResponse>() {
            override fun areItemsTheSame(old: CommentResponse, new: CommentResponse) = old.id == new.id
            override fun areContentsTheSame(old: CommentResponse, new: CommentResponse) = old == new
        }
    }
}
