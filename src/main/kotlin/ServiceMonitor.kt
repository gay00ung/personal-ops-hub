package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.time.Duration
import kotlin.io.path.getLastModifiedTime
import kotlin.system.measureTimeMillis

class ServiceMonitor(private val config: AppConfig) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    suspend fun runAll(): List<ServiceCheckResult> = buildList {
        config.checks.httpChecks.mapTo(this) { checkHttp(it) }
        config.checks.tcpChecks.mapTo(this) { checkTcp(it) }
        config.checks.dockerContainers.mapTo(this) { checkDockerContainer(it) }
        config.checks.backupMarkers.mapTo(this) { checkBackupMarker(it) }
    }

    private suspend fun checkHttp(check: HttpCheckConfig): ServiceCheckResult =
        withContext(Dispatchers.IO) {
            var status = HealthStatus.DOWN
            var message = "request failed"
            val elapsed = measureTimeMillis {
                runCatching {
                    val request = HttpRequest.newBuilder(URI.create(check.url))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build()
                    val response = client.send(request, HttpResponse.BodyHandlers.discarding())
                    status = if (response.statusCode() in 200..399) HealthStatus.UP else HealthStatus.DOWN
                    message = "HTTP ${response.statusCode()}"
                }.onFailure { error ->
                    message = error.message ?: error::class.simpleName.orEmpty().ifBlank { "request failed" }
                }
            }
            ServiceCheckResult(check.name, CheckKind.HTTP, status, elapsed, System.currentTimeMillis(), message)
        }

    private suspend fun checkTcp(check: TcpCheckConfig): ServiceCheckResult =
        withContext(Dispatchers.IO) {
            var status = HealthStatus.DOWN
            var message = "connection failed"
            val elapsed = measureTimeMillis {
                runCatching {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(check.host, check.port), 3000)
                    }
                    status = HealthStatus.UP
                    message = "port ${check.port} open"
                }.onFailure { error ->
                    message = error.message ?: error::class.simpleName.orEmpty().ifBlank { "connection failed" }
                }
            }
            ServiceCheckResult(check.name, CheckKind.TCP, status, elapsed, System.currentTimeMillis(), message)
        }

    private suspend fun checkDockerContainer(container: String): ServiceCheckResult =
        withContext(Dispatchers.IO) {
            val result = runCommand(listOf("docker", "inspect", "-f", "{{.State.Running}}", container), timeoutSeconds = 3)
            val running = result.success && result.output.trim().equals("true", ignoreCase = true)
            ServiceCheckResult(
                name = container,
                kind = CheckKind.DOCKER,
                status = if (running) HealthStatus.UP else HealthStatus.DOWN,
                latencyMs = null,
                checkedAt = System.currentTimeMillis(),
                message = if (running) "container running" else result.output.ifBlank { "container not running" },
            )
        }

    private suspend fun checkBackupMarker(marker: BackupMarkerConfig): ServiceCheckResult =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (!Files.exists(marker.path)) {
                return@withContext ServiceCheckResult(
                    name = marker.name,
                    kind = CheckKind.BACKUP,
                    status = HealthStatus.DOWN,
                    latencyMs = null,
                    checkedAt = now,
                    message = "backup marker missing: ${marker.path}",
                )
            }

            val modified = marker.path.getLastModifiedTime().toMillis()
            val ageMinutes = (now - modified) / 60_000
            ServiceCheckResult(
                name = marker.name,
                kind = CheckKind.BACKUP,
                status = if (ageMinutes <= marker.maxAgeMinutes) HealthStatus.UP else HealthStatus.DOWN,
                latencyMs = null,
                checkedAt = now,
                message = "last success ${ageMinutes}m ago",
            )
        }
}

suspend fun runCommand(
    command: List<String>,
    workingDirectory: java.io.File? = null,
    timeoutSeconds: Long = 120,
): CommandResult =
    withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder(command)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().readText().takeLast(8000)
            if (!finished) {
                process.destroyForcibly()
                CommandResult(false, null, output.ifBlank { "command timed out after ${timeoutSeconds}s" })
            } else {
                CommandResult(process.exitValue() == 0, process.exitValue(), output)
            }
        }.getOrElse { error ->
            CommandResult(false, null, error.message ?: error::class.simpleName.orEmpty())
        }
    }
