package com.example.instagramclone

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostApiTest {

    @Test
    fun `fotoğraflı gönderi oluşturunca 201 ve gönderi bilgisi döner`() = testApp {
        val token = registerAndLogin(username = "selim")

        val response = createPost(token, caption = "  İlk gönderim 📸  ")

        assertEquals(HttpStatusCode.Created, response.status)
        val post = response.body<JsonObject>()
        assertEquals("İlk gönderim 📸", post["caption"]!!.jsonPrimitive.content)
        assertEquals("selim", post["author"]!!.jsonObject["username"]!!.jsonPrimitive.content)
        assertTrue(post["imageUrl"]!!.jsonPrimitive.content.matches(Regex("/uploads/[0-9a-f-]{36}\\.png")))
    }

    @Test
    fun `yüklenen fotoğraf kendi adresinden aynen geri okunabilir`() = testApp {
        val token = registerAndLogin()
        val imageUrl = createPost(token).body<JsonObject>()["imageUrl"]!!.jsonPrimitive.content

        val response = client.get(imageUrl)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Image.PNG, response.contentType()?.withoutParameters())
        assertContentEquals(TEST_PNG, response.bodyAsBytes())
    }

    @Test
    fun `akış gönderileri en yeniden eskiye sıralar`() = testApp {
        val token = registerAndLogin()
        createPost(token, caption = "birinci")
        createPost(token, caption = "ikinci")
        createPost(token, caption = "üçüncü")

        val feed = client.get("/posts") { bearerAuth(token) }.body<JsonArray>()

        assertEquals(listOf("üçüncü", "ikinci", "birinci"), feed.map { it.jsonObject["caption"]!!.jsonPrimitive.content })
    }

    @Test
    fun `akış farklı kullanıcıların gönderilerini birlikte gösterir`() = testApp {
        val selim = registerAndLogin(username = "selim")
        val amad = registerAndLogin(username = "amad")
        createPost(selim, caption = "selim'den")
        createPost(amad, caption = "amad'dan")

        val feed = client.get("/posts") { bearerAuth(selim) }.body<JsonArray>()

        assertEquals(
            listOf("amad", "selim"),
            feed.map { it.jsonObject["author"]!!.jsonObject["username"]!!.jsonPrimitive.content },
        )
    }

    @Test
    fun `fotoğrafsız gönderi 400 döner`() = testApp {
        val token = registerAndLogin()

        val response = createPost(token, image = null)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Bir fotoğraf seçmelisin", response.errorMessage())
    }

    @Test
    fun `resim olmayan dosya 400 döner ve diske hiçbir şey yazılmaz`() = testApp {
        val token = registerAndLogin()

        val response = createPost(token, image = "merhaba".toByteArray(), imageType = "text/plain")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Sadece JPEG, PNG veya WEBP yüklenebilir", response.errorMessage())
        assertTrue(uploadDir.listFiles().isNullOrEmpty())
    }

    @Test
    fun `2200 karakterden uzun açıklama 400 döner ve yüklenen dosya silinir`() = testApp {
        val token = registerAndLogin()

        val response = createPost(token, caption = "a".repeat(2201))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(uploadDir.listFiles().isNullOrEmpty())
    }

    @Test
    fun `gönderi oluşturmak ve akışı görmek giriş gerektirir`() = testApp {
        assertEquals(HttpStatusCode.Unauthorized, client.get("/posts").status)
        assertEquals(HttpStatusCode.Unauthorized, createPost(token = "abc.def.ghi").status)
    }
}
