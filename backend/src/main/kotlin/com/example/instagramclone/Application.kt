package com.example.instagramclone

import com.example.instagramclone.db.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    routing {
        get("/") {
            call.respondText("Hello")
        }

        // Geçici: veritabanı bağlantısını tarayıcıdan kontrol etmek için.
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
    }
}
