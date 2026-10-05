package com.example.instagramclone.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.instagramclone.models.User
import java.time.Instant
import java.time.temporal.ChronoUnit

class TokenService(secret: String) {


    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generate(user: User): String = JWT.create()
        .withIssuer(ISSUER)
        .withSubject(user.id.toString())
        .withExpiresAt(Instant.now().plus(TOKEN_LIFETIME_DAYS, ChronoUnit.DAYS))
        .sign(algorithm)

    private companion object {
        const val ISSUER = "instagram-clone"
        const val TOKEN_LIFETIME_DAYS = 7L
    }
}
