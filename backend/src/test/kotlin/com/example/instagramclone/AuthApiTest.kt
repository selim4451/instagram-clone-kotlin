package com.example.instagramclone

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthApiTest {

    @Test
    fun `kayıt olunca 201 ve kullanıcı bilgisi döner, şifre hash'i dönmez`() = testApp {
        val response = register()

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertEquals("selim", body["username"]!!.jsonPrimitive.content)
        assertEquals("selim@example.com", body["email"]!!.jsonPrimitive.content)
        assertFalse("passwordHash" in body)
        assertFalse("password" in body)
    }

    @Test
    fun `kullanıcı adı ve e-posta küçük harfe çevrilip boşlukları kırpılarak kaydedilir`() = testApp {
        val body = register(username = "  Selim ", email = " Selim@Example.COM ").body<JsonObject>()

        assertEquals("selim", body["username"]!!.jsonPrimitive.content)
        assertEquals("selim@example.com", body["email"]!!.jsonPrimitive.content)
    }

    @Test
    fun `şifre veritabanına düz metin olarak değil bcrypt hash olarak yazılır`() = testApp {
        register(password = "gizli123")

        val hash = TestDatabase.queryString("SELECT password_hash FROM users WHERE username = ?", "selim")!!
        assertNotEquals("gizli123", hash)
        assertTrue(hash.startsWith("\$2a\$12\$"))
    }

    @Test
    fun `aynı kullanıcı adıyla ikinci kayıt 409 döner`() = testApp {
        register(username = "selim", email = "bir@example.com")

        val response = register(username = "selim", email = "iki@example.com")

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("Bu kullanıcı adı zaten alınmış", response.errorMessage())
    }

    @Test
    fun `aynı e-postayla ikinci kayıt 409 döner`() = testApp {
        register(username = "bir", email = "selim@example.com")

        val response = register(username = "iki", email = "SELIM@example.com")

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("Bu e-posta zaten kayıtlı", response.errorMessage())
    }

    @Test
    fun `geçersiz kullanıcı adı 400 döner`() = testApp {
        for (username in listOf("ab", "türkçe", "boşluk var", "a".repeat(31))) {
            val response = register(username = username)
            assertEquals(HttpStatusCode.BadRequest, response.status, "kullanıcı adı: $username")
        }
    }

    @Test
    fun `geçersiz e-posta 400 döner`() = testApp {
        val response = register(email = "e-posta-degil")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Geçerli bir e-posta gir", response.errorMessage())
    }

    @Test
    fun `6 karakterden kısa şifre 400 döner`() = testApp {
        val response = register(password = "12345")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Şifre en az 6, en fazla 72 karakter olmalı", response.errorMessage())
    }

    @Test
    fun `bozuk JSON gövdesi 400 döner`() = testApp {
        val response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("{\"username\":")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("İstek gövdesi geçersiz", response.errorMessage())
    }

    @Test
    fun `doğru bilgilerle giriş token ve kullanıcı döner`() = testApp {
        register()

        val response = login()

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertTrue(body["token"]!!.jsonPrimitive.content.split(".").size == 3)
        assertEquals("selim", (body["user"] as JsonObject)["username"]!!.jsonPrimitive.content)
    }

    @Test
    fun `girişte e-postanın büyük-küçük harfi önemli değildir`() = testApp {
        register(email = "selim@example.com")

        assertEquals(HttpStatusCode.OK, login(email = "SELIM@Example.com").status)
    }

    @Test
    fun `yanlış şifre ve kayıtlı olmayan e-posta aynı 401 mesajını döner`() = testApp {
        register()

        val wrongPassword = login(password = "yanlis123")
        val unknownEmail = login(email = "yok@example.com")

        assertEquals(HttpStatusCode.Unauthorized, wrongPassword.status)
        assertEquals(HttpStatusCode.Unauthorized, unknownEmail.status)
        assertEquals("E-posta veya şifre hatalı", wrongPassword.errorMessage())
        assertEquals(wrongPassword.errorMessage(), unknownEmail.errorMessage())
    }

    @Test
    fun `me geçerli token ile giriş yapan kullanıcıyı döner`() = testApp {
        val token = registerAndLogin(username = "selim")

        val response = client.get("/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("selim", response.body<JsonObject>()["username"]!!.jsonPrimitive.content)
    }

    @Test
    fun `me token olmadan ya da sahte token ile 401 döner`() = testApp {
        val withoutToken = client.get("/me")
        val fakeToken = client.get("/me") { bearerAuth("abc.def.ghi") }

        assertEquals(HttpStatusCode.Unauthorized, withoutToken.status)
        assertEquals(HttpStatusCode.Unauthorized, fakeToken.status)
        assertEquals("Oturum geçersiz ya da süresi dolmuş", withoutToken.errorMessage())
    }

    @Test
    fun `silinmiş kullanıcının token'ı ile me 401 döner`() = testApp {
        val token = registerAndLogin(username = "selim")
        TestDatabase.reset()

        assertEquals(HttpStatusCode.Unauthorized, client.get("/me") { bearerAuth(token) }.status)
    }
}
