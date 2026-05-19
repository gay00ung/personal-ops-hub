package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.parsers.DocumentBuilderFactory

class AutomationRunner(
    private val config: AppConfig,
    private val database: OpsDatabase,
    private val metricsCollector: SystemMetricsCollector,
    private val notifier: Notifier,
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    fun summary(): AutomationSummary =
        AutomationSummary(
            dailyReportTime = "%02d:%02d %s".format(
                config.automations.dailyReportHour,
                config.automations.dailyReportMinute,
                config.automations.zoneId.id,
            ),
            rssFeeds = config.automations.rssFeeds.map { NamedUrlDto(it.name, it.url) },
            pageWatches = config.automations.pageWatches.map { NamedUrlDto(it.name, it.url) },
            deployConfigured = !config.automations.deployCommand.isNullOrBlank(),
            alertTargetsConfigured = notifier.configuredTargets(),
        )

    suspend fun runPeriodicTasks() {
        runDailyReportIfDue()
        pollRssFeeds()
        pollPageWatches()
    }

    suspend fun recordBackupReport(request: BackupReportRequest): EventRecord {
        val severity = if (request.success) EventSeverity.INFO else EventSeverity.CRITICAL
        val message = request.message?.takeIf { it.isNotBlank() }
            ?: if (request.success) "backup succeeded" else "backup failed"
        val event = database.insertEvent(severity, "backup:${request.name}", message)
        if (!request.success) notifier.send("Backup failed", "${request.name}: $message")
        return event
    }

    suspend fun handleGithubWebhook(payload: String, signature: String?): CommandResult {
        val secret = config.automations.githubWebhookSecret
            ?: return CommandResult(false, null, "OPS_GITHUB_WEBHOOK_SECRET is not configured")
        val command = config.automations.deployCommand
            ?: return CommandResult(false, null, "OPS_DEPLOY_COMMAND is not configured")

        if (!verifyGithubSignature(payload, signature, secret)) {
            database.insertEvent(EventSeverity.WARNING, "deploy", "GitHub webhook rejected: invalid signature")
            return CommandResult(false, null, "invalid signature")
        }

        database.insertEvent(EventSeverity.INFO, "deploy", "GitHub webhook accepted")
        val result = runCommand(
            command = listOf("/bin/sh", "-lc", command),
            workingDirectory = config.automations.deployWorkingDirectory?.toFile(),
            timeoutSeconds = 180,
        )
        val severity = if (result.success) EventSeverity.INFO else EventSeverity.CRITICAL
        database.insertEvent(severity, "deploy", if (result.success) "deploy command succeeded" else "deploy command failed", result.output)
        if (!result.success) notifier.send("Deploy failed", result.output.ifBlank { "deploy command failed" })
        return result
    }

    private suspend fun runDailyReportIfDue() {
        val now = LocalDateTime.now(config.automations.zoneId)
        if (now.hour != config.automations.dailyReportHour || now.minute != config.automations.dailyReportMinute) return

        val today = LocalDate.now(config.automations.zoneId).toString()
        val key = "daily-report:$today"
        if (database.getState(key) == "sent") return

        val snapshot = metricsCollector.collect()
        val services = database.latestServiceChecks()
        val down = services.filter { it.status == HealthStatus.DOWN }
        val message = buildString {
            append("CPU ")
            append(snapshot.cpu.systemPercent?.let { "$it%" } ?: "n/a")
            append(", memory ")
            append(snapshot.memory.usedPercent?.let { "$it%" } ?: "n/a")
            append(", services down ")
            append(down.size)
            if (down.isNotEmpty()) append(": ${down.joinToString { it.name }}")
        }
        notifier.send("Daily server report", message)
        database.insertEvent(EventSeverity.INFO, "daily-report", message)
        database.setState(key, "sent")
    }

    private suspend fun pollRssFeeds() {
        for (feed in config.automations.rssFeeds) {
            val document = fetchXml(feed.url).getOrElse { error ->
                database.insertEvent(EventSeverity.WARNING, "rss:${feed.name}", error.message ?: "RSS fetch failed")
                continue
            }
            val items = extractFeedItems(document)
            val initializedKey = "rss:${feed.name}:initialized"
            val initialized = database.getState(initializedKey) == "true"
            for (item in items.take(5)) {
                val stateKey = "rss:${feed.name}:${sha256(item.id)}"
                if (database.getState(stateKey) == "seen") continue
                database.setState(stateKey, "seen")
                if (!initialized) continue
                val message = "${item.title} - ${item.link}"
                database.insertEvent(EventSeverity.INFO, "rss:${feed.name}", message)
                notifier.send("RSS update: ${feed.name}", message)
            }
            if (!initialized) {
                database.setState(initializedKey, "true")
                database.insertEvent(EventSeverity.INFO, "rss:${feed.name}", "feed initialized with ${items.size} items")
            }
        }
    }

    private suspend fun pollPageWatches() {
        for (watch in config.automations.pageWatches) {
            val body = fetchText(watch.url).getOrElse { error ->
                database.insertEvent(EventSeverity.WARNING, "page:${watch.name}", error.message ?: "page fetch failed")
                continue
            }
            val hash = sha256(body)
            val key = "page-watch:${watch.name}:hash"
            val previous = database.getState(key)
            database.setState(key, hash)
            if (previous != null && previous != hash) {
                val message = "content changed: ${watch.url}"
                database.insertEvent(EventSeverity.WARNING, "page:${watch.name}", message)
                notifier.send("Page changed: ${watch.name}", message)
            }
        }
    }

    private suspend fun fetchText(url: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() !in 200..399) error("HTTP ${response.statusCode()}")
                response.body()
            }
        }

    private suspend fun fetchXml(url: String): Result<org.w3c.dom.Document> =
        fetchText(url).mapCatching { xml ->
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            factory.isExpandEntityReferences = false
            factory.newDocumentBuilder().parse(xml.byteInputStream())
        }

    private fun extractFeedItems(document: org.w3c.dom.Document): List<FeedItem> {
        val itemNodes = document.getElementsByTagName("item")
        val rssItems = (0 until itemNodes.length).mapNotNull { index ->
            val element = itemNodes.item(index) as? Element ?: return@mapNotNull null
            val title = element.text("title")
            val link = element.text("link")
            val guid = element.text("guid")
            if (title.isBlank() && link.isBlank()) null else FeedItem(guid.ifBlank { link.ifBlank { title } }, title, link)
        }
        if (rssItems.isNotEmpty()) return rssItems

        val entryNodes = document.getElementsByTagName("entry")
        return (0 until entryNodes.length).mapNotNull { index ->
            val element = entryNodes.item(index) as? Element ?: return@mapNotNull null
            val title = element.text("title")
            val id = element.text("id")
            val link = (0 until element.getElementsByTagName("link").length)
                .mapNotNull { element.getElementsByTagName("link").item(it) as? Element }
                .firstOrNull()
                ?.getAttribute("href")
                .orEmpty()
            if (title.isBlank() && link.isBlank()) null else FeedItem(id.ifBlank { link.ifBlank { title } }, title, link)
        }
    }

    private fun verifyGithubSignature(payload: String, signature: String?, secret: String): Boolean {
        val expected = "sha256=" + hmacSha256(secret, payload)
        val actual = signature.orEmpty()
        return MessageDigest.isEqual(expected.toByteArray(), actual.toByteArray())
    }

    private fun hmacSha256(secret: String, payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun Element.text(tagName: String): String =
        getElementsByTagName(tagName).item(0)?.textContent?.trim().orEmpty()

    private data class FeedItem(
        val id: String,
        val title: String,
        val link: String,
    )
}
