package com.example.instagramclone.repositories

import com.example.instagramclone.ConflictException
import com.example.instagramclone.models.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.util.PSQLException
import javax.sql.DataSource

class UserRepository(private val dataSource: DataSource) {

    suspend fun create(username: String, email: String, passwordHash: String): UserResponse =
        withContext(Dispatchers.IO) {
            val sql = """
                INSERT INTO users (username, email, password_hash)
                VALUES (?, ?, ?)
                RETURNING id, username, email
            """.trimIndent()

            try {
                dataSource.connection.use { connection ->
                    connection.prepareStatement(sql).use { statement ->
                        statement.setString(1, username)
                        statement.setString(2, email)
                        statement.setString(3, passwordHash)
                        statement.executeQuery().use { result ->
                            result.next()
                            UserResponse(
                                id = result.getLong("id"),
                                username = result.getString("username"),
                                email = result.getString("email"),
                            )
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

    private companion object {
        const val UNIQUE_VIOLATION = "23505"  //Unique constraint ihlali bu kayıt zaten var demektir
    }
}
