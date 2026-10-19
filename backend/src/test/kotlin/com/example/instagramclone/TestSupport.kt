package com.example.instagramclone

import com.example.instagramclone.db.DatabaseFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import javax.sql.DataSource
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

object TestDatabase {

    val dataSource: DataSource by lazy {
        EmbeddedPostgres.builder()
            .setLocaleConfig("locale", "C")
            .setLocaleConfig("encoding", "UTF8")
            .start()
            .postgresDatabase
            .also(DatabaseFactory::createSchema)
    }

    fun reset() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { it.execute("TRUNCATE users, posts RESTART IDENTITY CASCADE") }
        }
    }

    fun queryString(sql: String, vararg params: Any): String? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                params.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
                statement.executeQuery().use { if (it.next()) it.getString(1) else null }
            }
        }
}

class TestApp(val client: HttpClient, val uploadDir: File)

fun testApp(block: suspend TestApp.() -> Unit) {
    TestDatabase.reset()
    val uploadDir = Files.createTempDirectory("uploads").toFile()
    try {
        testApplication {
            application { configure(TestDatabase.dataSource, TEST_JWT_SECRET, uploadDir) }
            TestApp(jsonClient(), uploadDir).block()
        }
    } finally {
        uploadDir.deleteRecursively()
    }
}

private fun ApplicationTestBuilder.jsonClient() = createClient {
    install(ClientContentNegotiation) { json() }
}

const val TEST_JWT_SECRET = "test-secret"

suspend fun TestApp.register(
    username: String = "selim",
    email: String = "selim@example.com",
    password: String = "gizli123",
): HttpResponse = client.post("/register") {
    contentType(ContentType.Application.Json)
    setBody(mapOf("username" to username, "email" to email, "password" to password))
}

suspend fun TestApp.login(email: String = "selim@example.com", password: String = "gizli123"): HttpResponse =
    client.post("/login") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("email" to email, "password" to password))
    }

suspend fun TestApp.registerAndLogin(
    username: String = "selim",
    email: String = "$username@example.com",
): String {
    register(username = username, email = email)
    return login(email = email).body<JsonObject>()["token"]!!.jsonPrimitive.content
}

suspend fun HttpResponse.errorMessage(): String = body<JsonObject>()["error"]!!.jsonPrimitive.content

val TEST_PNG: ByteArray = java.util.Base64.getDecoder().decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg=="
)
