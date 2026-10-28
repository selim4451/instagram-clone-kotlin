package com.example.instagramclone

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentApiTest {

    private suspend fun TestApp.addComment(token: String, postId: Long, text: String): HttpResponse =
        client.post("/posts/$postId/comments") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(mapOf("text" to text))
        }

    private suspend fun TestApp.comments(token: String, postId: Long): JsonArray =
        client.get("/posts/$postId/comments") { bearerAuth(token) }.body()

    private fun JsonArray.texts() = map { it.jsonObject["text"]!!.jsonPrimitive.content }

    private suspend fun TestApp.commentId(token: String, postId: Long, text: String): Long =
        addComment(token, postId, text).body<JsonObject>()["id"]!!.jsonPrimitive.content.toLong()

    @Test
    fun `yorum eklenince 201 döner ve yazanın adıyla listelenir`() = testApp {
        val token = registerAndLogin(username = "selim")
        val postId = createPostId(token)

        val response = addComment(token, postId, "  Harika olmuş 😍  ")

        assertEquals(HttpStatusCode.Created, response.status)
        val comment = comments(token, postId).single().jsonObject
        assertEquals("Harika olmuş 😍", comment["text"]!!.jsonPrimitive.content)
        assertEquals("selim", comment["author"]!!.jsonObject["username"]!!.jsonPrimitive.content)
    }

    @Test
    fun `yorumlar eskiden yeniye sıralanır ve akıştaki sayı güncellenir`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)
        addComment(token, postId, "birinci")
        addComment(token, postId, "ikinci")

        assertEquals(listOf("birinci", "ikinci"), comments(token, postId).texts())
        val post = client.get("/posts") { bearerAuth(token) }.body<JsonArray>().first().jsonObject
        assertEquals(2, post["commentCount"]!!.jsonPrimitive.int)
    }

    @Test
    fun `boş ya da sadece boşluktan oluşan yorum 400 döner`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)

        val response = addComment(token, postId, "   ")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Yorum boş olamaz", response.errorMessage())
    }

    @Test
    fun `2200 karakterden uzun yorum 400 döner`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)

        assertEquals(HttpStatusCode.BadRequest, addComment(token, postId, "a".repeat(2201)).status)
    }

    @Test
    fun `olmayan gönderiye yorum 404 döner`() = testApp {
        val token = registerAndLogin()

        assertEquals(HttpStatusCode.NotFound, addComment(token, 999, "merhaba").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/posts/999/comments") { bearerAuth(token) }.status)
    }

    @Test
    fun `kişi kendi yorumunu silebilir`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)
        val commentId = commentId(token, postId, "silinecek")

        val response = client.delete("/comments/$commentId") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertTrue(comments(token, postId).isEmpty())
    }

    @Test
    fun `gönderi sahibi kendi gönderisindeki başkasının yorumunu silebilir`() = testApp {
        val owner = registerAndLogin(username = "selim")
        val other = registerAndLogin(username = "amad")
        val postId = createPostId(owner)
        val commentId = commentId(other, postId, "amad'ın yorumu")

        assertTrue(comments(owner, postId).single().jsonObject["canDelete"]!!.jsonPrimitive.boolean)
        assertEquals(HttpStatusCode.NoContent, client.delete("/comments/$commentId") { bearerAuth(owner) }.status)
    }

    @Test
    fun `başkasının gönderisindeki başkasının yorumu silinemez`() = testApp {
        val owner = registerAndLogin(username = "selim")
        val commenter = registerAndLogin(username = "amad")
        val stranger = registerAndLogin(username = "ayse")
        val postId = createPostId(owner)
        val commentId = commentId(commenter, postId, "amad'ın yorumu")

        assertFalse(comments(stranger, postId).single().jsonObject["canDelete"]!!.jsonPrimitive.boolean)
        val response = client.delete("/comments/$commentId") { bearerAuth(stranger) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("Bu yorumu silemezsin", response.errorMessage())
        assertEquals(1, comments(owner, postId).size)
    }

    @Test
    fun `olmayan yorumu silmek 404 döner`() = testApp {
        val token = registerAndLogin()

        assertEquals(HttpStatusCode.NotFound, client.delete("/comments/999") { bearerAuth(token) }.status)
    }
}
