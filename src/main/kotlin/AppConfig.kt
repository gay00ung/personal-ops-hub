package net.lateinint

import java.nio.file.Path
import java.time.ZoneId
import kotlin.io.path.Path

data class AppConfig(
    val appName: String,
    val port: Int,
    val databasePath: Path,
    val retentionHours: Long,
    val collectionIntervalSeconds: Long,
    val auth: AuthConfig,
    val alerts: AlertConfig,
    val checks: ChecksConfig,
    val automations: AutomationConfig,
    val management: ManagementConfig,
)

data class AuthConfig(
    val username: String,
    val password: String?,
    val apiToken: String?,
) {
    val basicEnabled: Boolean get() = !password.isNullOrBlank()
    val tokenEnabled: Boolean get() = !apiToken.isNullOrBlank()
}

data class AlertConfig(
    val discordWebhookUrl: String?,
    val telegramBotToken: String?,
    val telegramChatId: String?,
    val diskCriticalPercent: Double,
    val memoryCriticalPercent: Double,
    val cpuCriticalPercent: Double,
)

data class ChecksConfig(
    val httpChecks: List<HttpCheckConfig>,
    val tcpChecks: List<TcpCheckConfig>,
    val dockerContainers: List<String>,
    val backupMarkers: List<BackupMarkerConfig>,
    val diskPaths: List<Path>,
)

data class HttpCheckConfig(
    val name: String,
    val url: String,
)

data class TcpCheckConfig(
    val name: String,
    val host: String,
    val port: Int,
)

data class BackupMarkerConfig(
    val name: String,
    val path: Path,
    val maxAgeMinutes: Long,
)

data class AutomationConfig(
    val dailyReportHour: Int,
    val dailyReportMinute: Int,
    val zoneId: ZoneId,
    val githubWebhookSecret: String?,
    val deployCommand: String?,
    val deployWorkingDirectory: Path?,
    val rssFeeds: List<NamedUrlConfig>,
    val pageWatches: List<NamedUrlConfig>,
)

data class ManagementConfig(
    val enabled: Boolean,
    val allowedSystemdUnits: List<String>,
    val restartOnlySystemdUnits: Set<String>,
    val allowedDockerContainers: List<String>,
)

data class NamedUrlConfig(
    val name: String,
    val url: String,
)

fun loadAppConfig(env: Map<String, String> = System.getenv()): AppConfig {
    val port = env["PORT"]?.toIntOrNull()
        ?: env["OPS_PORT"]?.toIntOrNull()
        ?: 8080

    return AppConfig(
        appName = env["OPS_APP_NAME"].orEmpty().ifBlank { "Personal Ops Hub" },
        port = port,
        databasePath = Path(setting(env, "OPS_DB_PATH").orEmpty().ifBlank { "data/ops-hub.db" }),
        retentionHours = env["OPS_RETENTION_HOURS"]?.toLongOrNull()?.coerceAtLeast(1) ?: 24,
        collectionIntervalSeconds = env["OPS_COLLECTION_INTERVAL_SECONDS"]?.toLongOrNull()?.coerceAtLeast(5) ?: 30,
        auth = AuthConfig(
            username = env["OPS_ADMIN_USER"].orEmpty().ifBlank { "admin" },
            password = env["OPS_ADMIN_PASSWORD"]?.takeIf { it.isNotBlank() },
            apiToken = env["OPS_ADMIN_TOKEN"]?.takeIf { it.isNotBlank() },
        ),
        alerts = AlertConfig(
            discordWebhookUrl = setting(env, "OPS_DISCORD_WEBHOOK_URL")?.takeIf { it.isNotBlank() },
            telegramBotToken = setting(env, "OPS_TELEGRAM_BOT_TOKEN")?.takeIf { it.isNotBlank() },
            telegramChatId = setting(env, "OPS_TELEGRAM_CHAT_ID")?.takeIf { it.isNotBlank() },
            diskCriticalPercent = env["OPS_DISK_CRITICAL_PERCENT"]?.toDoubleOrNull() ?: 80.0,
            memoryCriticalPercent = env["OPS_MEMORY_CRITICAL_PERCENT"]?.toDoubleOrNull() ?: 90.0,
            cpuCriticalPercent = env["OPS_CPU_CRITICAL_PERCENT"]?.toDoubleOrNull() ?: 95.0,
        ),
        checks = ChecksConfig(
            httpChecks = parseNamedUrls(env["OPS_HTTP_CHECKS"])
                .map { HttpCheckConfig(it.name, it.url) }
                .ifEmpty { listOf(HttpCheckConfig("ops-hub", "http://127.0.0.1:$port/api/health")) },
            tcpChecks = parseTcpChecks(env["OPS_TCP_CHECKS"]),
            dockerContainers = splitList(env["OPS_DOCKER_CONTAINERS"]),
            backupMarkers = parseBackupMarkers(env["OPS_BACKUP_MARKERS"]),
            diskPaths = splitList(env["OPS_DISK_PATHS"]).map { Path(it) }.ifEmpty { listOf(Path("/")) },
        ),
        automations = AutomationConfig(
            dailyReportHour = env["OPS_DAILY_REPORT_HOUR"]?.toIntOrNull()?.coerceIn(0, 23) ?: 9,
            dailyReportMinute = env["OPS_DAILY_REPORT_MINUTE"]?.toIntOrNull()?.coerceIn(0, 59) ?: 0,
            zoneId = runCatching { ZoneId.of(env["OPS_TIME_ZONE"].orEmpty().ifBlank { ZoneId.systemDefault().id }) }
                .getOrDefault(ZoneId.systemDefault()),
            githubWebhookSecret = env["OPS_GITHUB_WEBHOOK_SECRET"]?.takeIf { it.isNotBlank() },
            deployCommand = env["OPS_DEPLOY_COMMAND"]?.takeIf { it.isNotBlank() },
            deployWorkingDirectory = env["OPS_DEPLOY_WORKDIR"]?.takeIf { it.isNotBlank() }?.let { Path(it) },
            rssFeeds = parseNamedUrls(env["OPS_RSS_FEEDS"]),
            pageWatches = parseNamedUrls(env["OPS_PAGE_WATCHES"]),
        ),
        management = ManagementConfig(
            enabled = parseBoolean(setting(env, "OPS_MANAGE_ENABLED"), default = false),
            allowedSystemdUnits = splitList(setting(env, "OPS_ALLOWED_SYSTEMD_UNITS"))
                .map(::normalizeSystemdUnitName)
                .distinct(),
            restartOnlySystemdUnits = (
                splitList(setting(env, "OPS_RESTART_ONLY_SYSTEMD_UNITS")) +
                    (setting(env, "OPS_SELF_SYSTEMD_UNIT") ?: "personal-ops-hub.service")
                )
                .map(::normalizeSystemdUnitName)
                .toSet(),
            allowedDockerContainers = splitList(setting(env, "OPS_ALLOWED_DOCKER_CONTAINERS")).distinct(),
        ),
    )
}

private fun setting(env: Map<String, String>, key: String): String? =
    System.getProperty(key)?.takeIf { it.isNotBlank() } ?: env[key]

private fun splitList(value: String?): List<String> =
    value.orEmpty()
        .split(',', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

private fun parseNamedUrls(value: String?): List<NamedUrlConfig> =
    splitList(value).mapNotNull { item ->
        val parts = item.split('=', limit = 2)
        val name = parts.getOrNull(0)?.trim().orEmpty()
        val url = parts.getOrNull(1)?.trim().orEmpty()
        if (name.isBlank() || url.isBlank()) null else NamedUrlConfig(name, url)
    }

private fun parseTcpChecks(value: String?): List<TcpCheckConfig> =
    splitList(value).mapNotNull { item ->
        val parts = item.split('=', limit = 2)
        val name = parts.getOrNull(0)?.trim().orEmpty()
        val target = parts.getOrNull(1)?.trim().orEmpty()
        val host = target.substringBeforeLast(':', missingDelimiterValue = "")
        val port = target.substringAfterLast(':', missingDelimiterValue = "").toIntOrNull()
        if (name.isBlank() || host.isBlank() || port == null) null else TcpCheckConfig(name, host, port)
    }

private fun parseBackupMarkers(value: String?): List<BackupMarkerConfig> =
    splitList(value).mapNotNull { item ->
        val parts = item.split('=', limit = 2)
        val name = parts.getOrNull(0)?.trim().orEmpty()
        val target = parts.getOrNull(1)?.trim().orEmpty()
        val path = target.substringBeforeLast(':', missingDelimiterValue = target).trim()
        val maxAge = target.substringAfterLast(':', missingDelimiterValue = "1440").toLongOrNull() ?: 1440
        if (name.isBlank() || path.isBlank()) null else BackupMarkerConfig(name, Path(path), maxAge)
    }

fun normalizeSystemdUnitName(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return trimmed
    return if (trimmed.endsWith(".service") || trimmed.endsWith(".timer")) trimmed else "$trimmed.service"
}

private fun parseBoolean(value: String?, default: Boolean): Boolean =
    when (value?.trim()?.lowercase()) {
        null, "" -> default
        "1", "true", "yes", "y", "on" -> true
        "0", "false", "no", "n", "off" -> false
        else -> default
    }
