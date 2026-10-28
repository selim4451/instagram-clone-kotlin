package com.example.instagramclone

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
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

class LikeApiTest {

    private suspend fun TestApp.feedPost(token: String): JsonObject =
        client.get("/posts") { bearerAuth(token) }.body<JsonArray>().first().jsonObject

    @Test
    fun `yeni gönderinin beğeni ve yorum sayısı sıfırdır`() = testApp {
        val token = registerAndLogin()
        createPostId(token)

        val post = feedPost(token)

        assertEquals(0, post["likeCount"]!!.jsonPrimitive.int)
        assertFalse(post["likedByMe"]!!.jsonPrimitive.boolean)
        assertEquals(0, post["commentCount"]!!.jsonPrimitive.int)
    }

    @Test
    fun `beğenince sayı artar ve akışta beğenildi olarak görünür`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)

        val response = client.post("/posts/$postId/like") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, response.body<JsonObject>()["likeCount"]!!.jsonPrimitive.int)
        assertTrue(feedPost(token)["likedByMe"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `aynı kişi iki kez beğenirse sayı bir kez artar`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)

        client.post("/posts/$postId/like") { bearerAuth(token) }
        val second = client.post("/posts/$postId/like") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, second.status)
        assertEquals(1, second.body<JsonObject>()["likeCount"]!!.jsonPrimitive.int)
    }

    @Test
    fun `beğeniyi geri alınca sayı azalır`() = testApp {
        val token = registerAndLogin()
        val postId = createPostId(token)
        client.post("/posts/$postId/like") { bearerAuth(token) }

        val response = client.delete("/posts/$postId/like") { bearerAuth(token) }

        val body = response.body<JsonObject>()
        assertEquals(0, body["likeCount"]!!.jsonPrimitive.int)
        assertFalse(body["likedByMe"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `farklı kullanıcıların beğenileri toplanır, beğendim bilgisi kişiye özeldir`() = testApp {
        val selim = registerAndLogin(username = "selim")
        val amad = registerAndLogin(username = "amad")
        val postId = createPostId(selim)

        client.post("/posts/$postId/like") { bearerAuth(selim) }
        client.post("/posts/$postId/like") { bearerAuth(amad) }
        client.delete("/posts/$postId/like") { bearerAuth(selim) }

        assertEquals(1, feedPost(selim)["likeCount"]!!.jsonPrimitive.int)
        assertFalse(feedPost(selim)["likedByMe"]!!.jsonPrimitive.boolean)
        assertTrue(feedPost(amad)["likedByMe"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `olmayan gönderiyi beğenmek 404 döner`() = testApp {
        val token = registerAndLogin()

        val response = client.post("/posts/999/like") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Gönderi bulunamadı", response.errorMessage())
    }

    @Test
    fun `beğenmek giriş gerektirir`() = testApp {
        val postId = createPostId(registerAndLogin())

        assertEquals(HttpStatusCode.Unauthorized, client.post("/posts/$postId/like").status)
    }
}
