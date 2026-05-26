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
enum class ManagementTargetType {
    SYSTEMD_UNIT,
    DOCKER_CONTAINER,
}

@Serializable
enum class ManagementAction {
    START,
    STOP,
    RESTART,
}

@Serializable
enum class LogTargetType {
    SYSTEMD_UNIT,
    DOCKER_CONTAINER,
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
    val rssFeeds: List<AutomationTargetSummary>,
    val pageWatches: List<AutomationTargetSummary>,
    val deployConfigured: Boolean,
    val alertTargetsConfigured: List<String>,
)

@Serializable
enum class AutomationTargetStatus {
    UNKNOWN,
    OK,
    WARNING,
}

@Serializable
data class AutomationTargetSummary(
    val name: String,
    val url: String,
    val status: AutomationTargetStatus,
    val checkedAt: Long? = null,
    val message: String? = null,
)

@Serializable
data class OpsInventorySnapshot(
    val timestamp: Long,
    val sections: List<InventorySection>,
    val problems: List<InventoryProblem>,
)

@Serializable
data class InventorySection(
    val key: String,
    val title: String,
    val source: String,
    val available: Boolean,
    val message: String? = null,
    val items: List<InventoryItem> = emptyList(),
)

@Serializable
data class InventoryItem(
    val kind: String,
    val name: String,
    val status: String? = null,
    val schedule: String? = null,
    val command: String? = null,
    val detail: String? = null,
    val raw: String? = null,
    val actions: List<ManagementAction> = emptyList(),
    val logs: List<LogTargetType> = emptyList(),
)

@Serializable
data class InventoryProblem(
    val severity: EventSeverity,
    val source: String,
    val message: String,
    val detail: String? = null,
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
data class ManagementActionRequest(
    val targetType: ManagementTargetType,
    val name: String,
    val action: ManagementAction,
)

@Serializable
data class ManagementActionResponse(
    val targetType: ManagementTargetType,
    val name: String,
    val action: ManagementAction,
    val success: Boolean,
    val exitCode: Int?,
    val output: String,
    val event: EventRecord,
)

@Serializable
data class LogReadResponse(
    val targetType: LogTargetType,
    val name: String,
    val lines: Int,
    val success: Boolean,
    val exitCode: Int?,
    val output: String,
    val timestamp: Long,
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
