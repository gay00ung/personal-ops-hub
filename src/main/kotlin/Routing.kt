package net.lateinint

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    val hub = opsHub()
    monitor.subscribe(ApplicationStarted) {
        hub.start()
    }
    monitor.subscribe(ApplicationStopping) {
        hub.stop()
    }

    routing {
        staticResources("/assets", "dashboard")

        get("/") {
            call.respondRedirect("/dashboard")
        }

        adminRoute(hub) {
            get("/dashboard") {
                call.respondText(
                    text = this::class.java.classLoader.getResource("dashboard/index.html")!!.readText(),
                    contentType = ContentType.Text.Html,
                )
            }
        }

        route("/api") {
            get("/health") {
                call.respond(hub.healthResponse())
            }

            adminRoute(hub) {
                get("/summary") {
                    call.respond(hub.dashboardSummary())
                }

                get("/metrics/current") {
                    call.respond(hub.currentSnapshot())
                }

                get("/metrics/history") {
                    val since = call.request.queryParameters["since"]?.toLongOrNull()
                        ?: (System.currentTimeMillis() - 60.minutesInMillis)
                    call.respond(MetricsHistoryResponse(hub.database.recentMetrics(since)))
                }

                get("/services") {
                    call.respond(hub.database.latestServiceChecks())
                }

                post("/services/run") {
                    if (!call.requireAdminToken(hub)) return@post
                    val snapshot = hub.collectOnce()
                    call.respond(snapshot)
                }

                get("/events") {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 200) ?: 100
                    call.respond(hub.database.recentEvents(limit))
                }

                get("/automation") {
                    call.respond(hub.automationRunner.summary())
                }

                post("/alerts/test") {
                    if (!call.requireAdminToken(hub)) return@post
                    val request = runCatching { call.receive<AlertTestRequest>() }.getOrDefault(AlertTestRequest())
                    hub.notifier.send("Test alert", request.message)
                    val event = hub.database.insertEvent(EventSeverity.INFO, "alert-test", request.message)
                    call.respond(event)
                }

                post("/backups/report") {
                    if (!call.requireAdminToken(hub)) return@post
                    val request = call.receive<BackupReportRequest>()
                    call.respond(hub.automationRunner.recordBackupReport(request))
                }
            }
        }

        post("/webhook/github") {
            val payload = call.receiveText()
            val signature = call.request.headers["X-Hub-Signature-256"]
            val result = hub.automationRunner.handleGithubWebhook(payload, signature)
            val status = when {
                result.success -> HttpStatusCode.OK
                result.output.contains("not configured") -> HttpStatusCode.ServiceUnavailable
                result.output.contains("invalid signature") -> HttpStatusCode.Unauthorized
                else -> HttpStatusCode.InternalServerError
            }
            call.respond(status, result)
        }

        adminRoute(hub) {
            webSocket("/ws/metrics") {
                while (true) {
                    val snapshot = hub.currentSnapshot()
                    send(Frame.Text(AppJson.encodeToString(SystemSnapshot.serializer(), snapshot)))
                    delay(5.seconds)
                }
            }
        }

        webSocket("/ws") {
            send(Frame.Text("Use /ws/metrics for live server metrics."))
            for (frame in incoming) {
                if (frame is Frame.Text && frame.readText().equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }
    }
}

private val Int.minutesInMillis: Long get() = this * 60_000L
