package com.example.instagramclone.routes

import com.example.instagramclone.dto.LoginRequest
import com.example.instagramclone.dto.RegisterRequest
import com.example.instagramclone.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val request = call.receive<RegisterRequest>()
        val user = authService.register(request)
        call.respond(HttpStatusCode.Created, user)
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        call.respond(authService.login(request))
    }

    authenticate(JWT_AUTH) {
        get("/me") {
            call.respond(authService.getUser(call.userId()))
        }
    }
}

const val JWT_AUTH = "auth-jwt"
