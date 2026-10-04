package com.example.instagramclone.routes

import com.example.instagramclone.models.RegisterRequest
import com.example.instagramclone.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val request = call.receive<RegisterRequest>()
        val user = authService.register(request)
        call.respond(HttpStatusCode.Created, user)
    }
}