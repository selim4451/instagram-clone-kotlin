package com.example.instagramclone

import io.ktor.http.HttpStatusCode


open class ApiException(val status: HttpStatusCode, message: String) : RuntimeException(message)

class ValidationException(message: String) : ApiException(HttpStatusCode.BadRequest, message)

class ConflictException(message: String) : ApiException(HttpStatusCode.Conflict, message)

class UnauthorizedException(message: String) : ApiException(HttpStatusCode.Unauthorized, message)

class ForbiddenException(message: String) : ApiException(HttpStatusCode.Forbidden, message)

class NotFoundException(message: String) : ApiException(HttpStatusCode.NotFound, message)
