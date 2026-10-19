package com.example.instagramclone

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.instagramclone.models.User
import com.example.instagramclone.services.TokenService
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenServiceTest {

    private val user = User(
        id = 42,
        username = "selim",
        email = "selim@example.com",
        passwordHash = "hash",
        createdAt = Instant.now(),
    )

    @Test
    fun `üretilen token doğrulanır ve içinde sadece kullanıcı id'si taşır`() {
        val service = TokenService("secret")

        val decoded = service.verifier.verify(service.generate(user))

        assertEquals("42", decoded.subject)
        assertTrue("email" !in decoded.claims && "username" !in decoded.claims)
    }

    @Test
    fun `token 7 gün sonra geçersiz olur`() {
        val decoded = JWT.decode(TokenService("secret").generate(user))

        val expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS)
        assertTrue(kotlin.math.abs(decoded.expiresAtAsInstant.epochSecond - expectedExpiry.epochSecond) < 5)
    }

    @Test
    fun `başka bir secret ile imzalanmış token reddedilir`() {
        val forged = TokenService("baska-secret").generate(user)

        assertFailsWith<JWTVerificationException> { TokenService("secret").verifier.verify(forged) }
    }
}
