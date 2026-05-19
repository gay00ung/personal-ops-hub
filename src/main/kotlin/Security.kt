package net.lateinint

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.principal
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import java.security.MessageDigest

fun Application.configureSecurity() {
    val hub = opsHub()
    install(Authentication) {
        basic("admin") {
            realm = hub.config.appName
            validate { credentials ->
                val password = hub.config.auth.password
                if (
                    password != null &&
                    credentials.name == hub.config.auth.username &&
                    constantTimeEquals(credentials.password, password)
                ) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

fun Route.adminRoute(hub: OpsHub, build: Route.() -> Unit) {
    if (hub.config.auth.basicEnabled) {
        authenticate("admin") {
            build()
        }
    } else {
        build()
    }
}

suspend fun ApplicationCall.requireAdminToken(hub: OpsHub): Boolean {
    if (hub.config.auth.basicEnabled && principal<UserIdPrincipal>() != null) return true
    val expected = hub.config.auth.apiToken ?: return true
    val bearer = request.header(HttpHeaders.Authorization)
        ?.removePrefix("Bearer ")
        ?.takeIf { it.isNotBlank() }
    val actual = request.header("X-Ops-Token") ?: bearer
    if (actual != null && constantTimeEquals(actual, expected)) return true

    respond(
        HttpStatusCode.Unauthorized,
        ErrorResponse("unauthorized", "missing or invalid admin token"),
    )
    return false
}

private fun constantTimeEquals(a: String, b: String): Boolean =
    MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
