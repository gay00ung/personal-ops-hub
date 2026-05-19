package net.lateinint

import kotlinx.serialization.Serializable

@Serializable
enum class HealthStatus {
    UP,
    DEGRADED,
    DOWN,
    UNKNOWN,
}

@Serializable
enum class EventSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

@Serializable
enum class EventState {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED,
}

@Serializable
enum class CheckKind {
    HTTP,
    TCP,
    DOCKER,
    BACKUP,
}

@Serializable
data class ApiHealthResponse(
    val status: HealthStatus,
    val app: String,
    val version: String,
    val timestamp: Long,
    val message: String,
)

@Serializable
data class SystemSnapshot(
    val timestamp: Long,
    val host: String,
    val uptimeSeconds: Long,
    val loadAverage: Double?,
    val cpu: CpuMetrics,
    val memory: MemoryMetrics,
    val jvm: JvmMetrics,
    val disks: List<DiskMetrics>,
)

@Serializable
data class CpuMetrics(
    val systemPercent: Double?,
    val processPercent: Double?,
    val availableProcessors: Int,
)

@Serializable
data class MemoryMetrics(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercent: Double?,
)

@Serializable
data class JvmMetrics(
    val maxBytes: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercent: Double?,
)

@Serializable
data class DiskMetrics(
    val path: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val usableBytes: Long,
    val usedPercent: Double?,
)

@Serializable
data class ServiceCheckResult(
    val name: String,
    val kind: CheckKind,
    val status: HealthStatus,
    val latencyMs: Long?,
    val checkedAt: Long,
    val message: String,
)

@Serializable
data class EventRecord(
    val id: Long,
    val timestamp: Long,
    val severity: EventSeverity,
    val source: String,
    val message: String,
    val details: String? = null,
    val state: EventState = EventState.OPEN,
    val actionRequired: Boolean = severity != EventSeverity.INFO,
)

@Serializable
data class EventStateUpdateRequest(
    val state: EventState,
)

@Serializable
data class MetricsHistoryResponse(
    val samples: List<SystemSnapshot>,
)

@Serializable
data class DashboardSummary(
    val health: ApiHealthResponse,
    val current: SystemSnapshot,
    val services: List<ServiceCheckResult>,
    val events: List<EventRecord>,
    val automation: AutomationSummary,
    val authEnabled: Boolean,
)

@Serializable
data class AutomationSummary(
    val dailyReportTime: String,
    val rssFeeds: List<NamedUrlDto>,
    val pageWatches: List<NamedUrlDto>,
    val deployConfigured: Boolean,
    val alertTargetsConfigured: List<String>,
)

@Serializable
data class NamedUrlDto(
    val name: String,
    val url: String,
)

@Serializable
data class BackupReportRequest(
    val name: String,
    val success: Boolean,
    val message: String? = null,
)

@Serializable
data class AlertTestRequest(
    val message: String = "Personal Ops Hub test alert",
)

@Serializable
data class CommandResult(
    val success: Boolean,
    val exitCode: Int?,
    val output: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
)
