package net.lateinint

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.response.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*

fun Application.configureHttp() {
    install(Compression)
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders)
}
