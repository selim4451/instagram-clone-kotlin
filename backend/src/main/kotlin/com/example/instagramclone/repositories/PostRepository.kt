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
                SELECT $COLUMNS FROM inserted p JOIN users u ON u.id = p.user_id
            """.trimIndent()

            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, userId)
                    statement.setString(2, imageName)
                    statement.setString(3, caption)
                    statement.executeQuery().use { result ->
                        result.next()
                        result.toPost()
                    }
                }
            }
        }

    suspend fun findLatest(limit: Int): List<Post> =
        withContext(Dispatchers.IO) {
            val sql = """
                SELECT $COLUMNS FROM posts p JOIN users u ON u.id = p.user_id
                ORDER BY p.created_at DESC, p.id DESC
                LIMIT ?
            """.trimIndent()

            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setInt(1, limit)
                    statement.executeQuery().use { result ->
                        buildList { while (result.next()) add(result.toPost()) }
                    }
                }
            }
        }

    private fun ResultSet.toPost() = Post(
        id = getLong("id"),
        authorId = getLong("user_id"),
        authorUsername = getString("username"),
        imageName = getString("image_name"),
        caption = getString("caption"),
        createdAt = getObject("created_at", OffsetDateTime::class.java).toInstant(),
    )

    private companion object {
        const val COLUMNS = "p.id, p.user_id, u.username, p.image_name, p.caption, p.created_at"
    }
}
