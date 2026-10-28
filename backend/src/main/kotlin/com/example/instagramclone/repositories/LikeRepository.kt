package com.example.instagramclone.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.DataSource

class LikeRepository(private val dataSource: DataSource) {

    suspend fun like(userId: Long, postId: Long) = execute(
        "INSERT INTO likes (user_id, post_id) VALUES (?, ?) ON CONFLICT DO NOTHING", userId, postId,
    )

    suspend fun unlike(userId: Long, postId: Long) = execute(
        "DELETE FROM likes WHERE user_id = ? AND post_id = ?", userId, postId,
    )

    suspend fun count(postId: Long): Int =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT count(*) FROM likes WHERE post_id = ?").use { statement ->
                    statement.setLong(1, postId)
                    statement.executeQuery().use { result ->
                        result.next()
                        result.getInt(1)
                    }
                }
            }
        }

    private suspend fun execute(sql: String, userId: Long, postId: Long) {
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, userId)
                    statement.setLong(2, postId)
                    statement.executeUpdate()
                }
            }
        }
    }
}
