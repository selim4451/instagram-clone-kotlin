package com.example.instagramclone.repositories

import com.example.instagramclone.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.sql.DataSource

class PostRepository(private val dataSource: DataSource) {

    suspend fun create(userId: Long, imageName: String, caption: String): Post =
        withContext(Dispatchers.IO) {
            val sql = """
                WITH inserted AS (
                    INSERT INTO posts (user_id, image_name, caption)
                    VALUES (?, ?, ?)
                    RETURNING *
                )
                SELECT $BASE_COLUMNS FROM inserted p JOIN users u ON u.id = p.user_id
            """.trimIndent()

            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, userId)
                    statement.setString(2, imageName)
                    statement.setString(3, caption)
                    statement.executeQuery().use { result ->
                        result.next()
                        result.toPost(withStats = false)
                    }
                }
            }
        }

    suspend fun findLatest(viewerId: Long, limit: Int): List<Post> =
        withContext(Dispatchers.IO) {
            val sql = """
                SELECT $BASE_COLUMNS, $STATS_COLUMNS
                FROM posts p JOIN users u ON u.id = p.user_id
                ORDER BY p.created_at DESC, p.id DESC
                LIMIT ?
            """.trimIndent()

            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, viewerId)
                    statement.setInt(2, limit)
                    statement.executeQuery().use { result ->
                        buildList { while (result.next()) add(result.toPost(withStats = true)) }
                    }
                }
            }
        }

    suspend fun exists(postId: Long): Boolean =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT 1 FROM posts WHERE id = ?").use { statement ->
                    statement.setLong(1, postId)
                    statement.executeQuery().use { it.next() }
                }
            }
        }

    private fun ResultSet.toPost(withStats: Boolean) = Post(
        id = getLong("id"),
        authorId = getLong("user_id"),
        authorUsername = getString("username"),
        imageName = getString("image_name"),
        caption = getString("caption"),
        createdAt = getObject("created_at", OffsetDateTime::class.java).toInstant(),
        likeCount = if (withStats) getInt("like_count") else 0,
        likedByMe = withStats && getBoolean("liked_by_me"),
        commentCount = if (withStats) getInt("comment_count") else 0,
    )

    private companion object {
        const val BASE_COLUMNS = "p.id, p.user_id, u.username, p.image_name, p.caption, p.created_at"
        const val STATS_COLUMNS = """
            (SELECT count(*) FROM likes l WHERE l.post_id = p.id) AS like_count,
            EXISTS (SELECT 1 FROM likes l WHERE l.post_id = p.id AND l.user_id = ?) AS liked_by_me,
            (SELECT count(*) FROM comments c WHERE c.post_id = p.id) AS comment_count
        """
    }
}
