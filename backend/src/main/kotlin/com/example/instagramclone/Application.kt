package com.example.instagramclone

import com.example.instagramclone.db.DatabaseFactory
import com.example.instagramclone.dto.ErrorResponse
import com.example.instagramclone.repositories.PostRepository
import com.example.instagramclone.repositories.UserRepository
import com.example.instagramclone.routes.JWT_AUTH
import com.example.instagramclone.routes.authRoutes
import com.example.instagramclone.routes.postRoutes
import com.example.instagramclone.services.AuthService
import com.example.instagramclone.services.PostService
import com.example.instagramclone.services.TokenService
import com.example.instagramclone.storage.ImageStorage
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File
import javax.sql.DataSource

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configure(
        dataSource = DatabaseFactory.createDataSource(),
        jwtSecret = System.getenv("JWT_SECRET") ?: error("JWT_SECRET environment variable is not set"),
        uploadDir = File(System.getenv("UPLOAD_DIR") ?: "uploads"),
    )
}

fun Application.configure(dataSource: DataSource, jwtSecret: String, uploadDir: File) {
    DatabaseFactory.createSchema(dataSource)
    val tokenService = TokenService(jwtSecret)
    val authService = AuthService(UserRepository(dataSource), tokenService)
    val imageStorage = ImageStorage(uploadDir)
    val postService = PostService(PostRepository(dataSource), imageStorage)

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt(JWT_AUTH) {
            verifier(tokenService.verifier)
            validate { credential ->
                if (credential.subject?.toLongOrNull() != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Oturum geçersiz ya da süresi dolmuş"))
            }
        }
    }

    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.message ?: "Hata"))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("İstek gövdesi geçersiz"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unexpected error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Sunucu hatası"))
        }
    }

    routing {
        get("/") {
            call.respondText("Hello")
        }

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/db-check") {
            val userCount = dataSource.connection.use { connection ->
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
        postRoutes(postService, imageStorage)

        staticFiles("/uploads", imageStorage.directory)
    }
}
