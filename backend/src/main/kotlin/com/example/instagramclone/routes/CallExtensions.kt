package com.example.instagramclone.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.userId(): Long = principal<JWTPrincipal>()!!.subject!!.toLong()
