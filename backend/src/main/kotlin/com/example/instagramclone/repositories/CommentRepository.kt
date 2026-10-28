package com.example.instagramclone.repositories

import com.example.instagramclone.models.Comment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.sql.DataSource

class CommentRepository(private val dataSource: DataSource) {

    suspend fun create(postId: Long, userId: Long, text: String): Comment {
        val id = withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO comments (post_id, user_id, text) VALUES (?, ?, ?) RETURNING id"
                ).use { statement ->
                    statement.setLong(1, postId)
                    statement.setLong(2, userId)
                    statement.setString(3, text)
                    statement.executeQuery().use { result ->
                        result.next()
                        result.getLong(1)
                    }
                }
            }
        }
        return checkNotNull(findById(id))
    }

    suspend fun findByPost(postId: Long): List<Comment> =
        query("$SELECT WHERE c.post_id = ? ORDER BY c.created_at, c.id") { setLong(1, postId) }

    suspend fun findById(commentId: Long): Comment? =
        query("$SELECT WHERE c.id = ?") { setLong(1, commentId) }.firstOrNull()

    suspend fun delete(commentId: Long) {
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement("DELETE FROM comments WHERE id = ?").use { statement ->
                    statement.setLong(1, commentId)
                    statement.executeUpdate()
                }
            }
        }
    }

    private suspend fun query(sql: String, bind: PreparedStatement.() -> Unit): List<Comment> =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.bind()
                    statement.executeQuery().use { result ->
                        buildList { while (result.next()) add(result.toComment()) }
                    }
                }
            }
        }

    private fun ResultSet.toComment() = Comment(
        id = getLong("id"),
        postId = getLong("post_id"),
        postAuthorId = getLong("post_author_id"),
        authorId = getLong("user_id"),
        authorUsername = getString("username"),
        text = getString("text"),
        createdAt = getObject("created_at", OffsetDateTime::class.java).toInstant(),
    )

    private companion object {
        const val SELECT = """
            SELECT c.id, c.post_id, p.user_id AS post_author_id, c.user_id, u.username, c.text, c.created_at
            FROM comments c
            JOIN users u ON u.id = c.user_id
            JOIN posts p ON p.id = c.post_id
        """
    }
}
