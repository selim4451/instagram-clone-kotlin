package com.example.instagramclone

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.instagramclone.data.remote.dto.PostResponse
import com.example.instagramclone.databinding.ItemPostBinding
import com.example.instagramclone.util.relativeTime

class PostAdapter(
    private val onLikeClick: (PostResponse) -> Unit,
    private val onDoubleTap: (PostResponse) -> Unit,
    private val onCommentsClick: (PostResponse) -> Unit,
) : ListAdapter<PostResponse, PostAdapter.PostViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        private var post: PostResponse? = null

        private val gestureDetector = GestureDetector(
            binding.root.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent) = true

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    post?.let {
                        showHeart()
                        onDoubleTap(it)
                    }
                    return true
                }
            },
        )

        init {
            binding.postImage.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
            binding.likeButton.setOnClickListener { post?.let(onLikeClick) }
            binding.commentButton.setOnClickListener { post?.let(onCommentsClick) }
            binding.viewCommentsText.setOnClickListener { post?.let(onCommentsClick) }
        }

        fun bind(post: PostResponse) {
            val context = binding.root.context
            val previous = this.post
            this.post = post

            binding.usernameText.text = post.author.username
            if (previous?.fullImageUrl != post.fullImageUrl) {
                binding.postImage.load(post.fullImageUrl)
            }

            binding.likeButton.setImageResource(
                if (post.likedByMe) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            binding.likeButton.contentDescription =
                context.getString(if (post.likedByMe) R.string.post_unlike else R.string.post_like)

            binding.likeCountText.isVisible = post.likeCount > 0
            binding.likeCountText.text =
                context.resources.getQuantityString(R.plurals.post_like_count, post.likeCount, post.likeCount)

            binding.captionText.isVisible = post.caption.isNotEmpty()
            binding.captionText.text = SpannableStringBuilder()
                .inSpans(StyleSpan(Typeface.BOLD)) { append(post.author.username) }
                .append(" ")
                .append(post.caption)

            binding.viewCommentsText.isVisible = post.commentCount > 0
            binding.viewCommentsText.text = context.resources.getQuantityString(
                R.plurals.post_view_comments, post.commentCount, post.commentCount
            )

            binding.timeText.text = relativeTime(context, post.createdAt)
        }

        private fun showHeart() {
            val heart = binding.doubleTapHeart
            heart.animate().cancel()
            heart.alpha = 0f
            heart.scaleX = 0.5f
            heart.scaleY = 0.5f
            heart.animate()
                .alpha(1f).scaleX(1.1f).scaleY(1.1f)
                .setStartDelay(0)
                .setDuration(180)
                .withEndAction {
                    heart.animate().alpha(0f).scaleX(1f).scaleY(1f).setStartDelay(350).setDuration(200)
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
