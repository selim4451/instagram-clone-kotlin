package com.example.instagramclone.routes

import com.example.instagramclone.ValidationException
import com.example.instagramclone.services.PostService
import com.example.instagramclone.storage.ImageStorage
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.postRoutes(postService: PostService, imageStorage: ImageStorage) {
    authenticate(JWT_AUTH) {
        get("/posts") {
            call.respond(postService.feed())
        }

        post("/posts") {
            var caption = ""
            var imageName: String? = null

            call.receiveMultipart(formFieldLimit = MAX_UPLOAD_BYTES).forEachPart { part ->
                when {
                    part is PartData.FormItem && part.name == "caption" -> caption = part.value
                    part is PartData.FileItem && part.name == "image" -> {
                        val extension = part.contentType?.let(::extensionFor)
                            ?: throw ValidationException("Sadece JPEG, PNG veya WEBP yüklenebilir")
                        imageName = imageStorage.save(part.provider(), extension)
                    }
                }
                part.dispose()
            }

            call.respond(HttpStatusCode.Created, postService.create(call.userId(), imageName, caption))
        }
    }
}

private fun extensionFor(type: ContentType): String? = when {
    type.match(ContentType.Image.JPEG) -> "jpg"
    type.match(ContentType.Image.PNG) -> "png"
    type.match(ContentType("image", "webp")) -> "webp"
    else -> null
}

private const val MAX_UPLOAD_BYTES = 10L * 1024 * 1024
