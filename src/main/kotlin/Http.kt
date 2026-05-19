package net.lateinint

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.response.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

fun Application.configureHttp() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(Compression)
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
        header("Referrer-Policy", "same-origin")
    }
    install(ForwardedHeaders)
    install(XForwardedHeaders)
}
