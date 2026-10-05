package com.example.instagramclone.repositories

import com.example.instagramclone.ConflictException
import com.example.instagramclone.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.util.PSQLException
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.sql.DataSource

class UserRepository(private val dataSource: DataSource) {

    suspend fun create(username: String, email: String, passwordHash: String): User =
        withContext(Dispatchers.IO) {
            val sql = """
                INSERT INTO users (username, email, password_hash)
                VALUES (?, ?, ?)
                RETURNING id, username, email, password_hash, created_at
            """.trimIndent()

            try {
                dataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        statement.setString(1, username)
                        statement.setString(2, email)
                        statement.setString(3, passwordHash)
                        statement.executeQuery().use { result ->
                            result.next()
                            result.toUser()
                        }
                    }
                }
            } catch (e: PSQLException) {
                if (e.sqlState != UNIQUE_VIOLATION) throw e
                throw when (e.serverErrorMessage?.constraint) {
                    "users_username_key" -> ConflictException("Bu kullanıcı adı zaten alınmış")
                    "users_email_key" -> ConflictException("Bu e-posta zaten kayıtlı")
                    else -> e
                }
            }
        }

    suspend fun findByEmail(email: String): User? =
        findOne("SELECT $COLUMNS FROM users WHERE email = ?") { setString(1, email) }

    suspend fun findById(id: Long): User? =
        findOne("SELECT $COLUMNS FROM users WHERE id = ?") { setLong(1, id) }

    private suspend fun findOne(sql: String, bind: PreparedStatement.() -> Unit): User? =
        withContext(Dispatchers.IO) {
            dataSource.connection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.bind()
                    statement.executeQuery().use { result ->
                        if (result.next()) result.toUser() else null
                    }
                }
            }
        }

    private fun ResultSet.toUser() = User(
        id = getLong("id"),
        username = getString("username"),
        email = getString("email"),
        passwordHash = getString("password_hash"),
        createdAt = getObject("created_at", OffsetDateTime::class.java).toInstant(),
    )

    private companion object {
        const val UNIQUE_VIOLATION = "23505"
        const val COLUMNS = "id, username, email, password_hash, created_at"
    }
}
