package com.example.instagramclone

import com.example.instagramclone.db.DatabaseFactory
import com.example.instagramclone.models.ErrorResponse
import com.example.instagramclone.repositories.UserRepository
import com.example.instagramclone.routes.authRoutes
import com.example.instagramclone.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    val authService = AuthService(UserRepository(DatabaseFactory.dataSource))

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.message ?: "Hata"))
        }
        // Bozuk ya da eksik alanlı JSON gövdesi.
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("İstek gövdesi geçersiz"))
        }
        // Beklenmeyen hatalar: ayrıntı loglanır, istemciye iç bilgi sızdırılmaz.
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Beklenmeyen hata", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Sunucu hatası"))
        }
    }

    routing {
        get("/") {
            call.respondText("Hello")
        }

        get("/db-check") {
            val userCount = DatabaseFactory.dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT COUNT(*) FROM users").use { statement ->
                    statement.executeQuery().use { result ->
                        result.next()
                        result.getLong(1)
                    }
                }
            }
            call.respondText("Veritabanı bağlı. Kullanıcı sayısı: $userCount")
        }

        authRoutes(authService)
    }
}
