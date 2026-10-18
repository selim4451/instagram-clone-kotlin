package com.example.instagramclone

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.databinding.ItemPostBinding

class PostAdapter : ListAdapter<PostResponse, PostAdapter.PostViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostResponse) {
            binding.usernameText.text = post.author.username
            binding.postImage.load(post.fullImageUrl)

            binding.captionText.isVisible = post.caption.isNotEmpty()
            binding.captionText.text = SpannableStringBuilder()
                .inSpans(StyleSpan(Typeface.BOLD)) { append(post.author.username) }
                .append(" ")
                .append(post.caption)

            val now = System.currentTimeMillis()
            binding.timeText.text = if (now - post.createdAt < DateUtils.MINUTE_IN_MILLIS) {
                binding.root.context.getString(R.string.time_just_now)
            } else {
                DateUtils.getRelativeTimeSpanString(post.createdAt, now, DateUtils.MINUTE_IN_MILLIS)
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<PostResponse>() {
            override fun areItemsTheSame(old: PostResponse, new: PostResponse) = old.id == new.id
            override fun areContentsTheSame(old: PostResponse, new: PostResponse) = old == new
        }
    }
}
