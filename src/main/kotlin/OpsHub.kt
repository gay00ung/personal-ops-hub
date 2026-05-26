package net.lateinint

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

private val OpsHubAttribute = AttributeKey<OpsHub>("OpsHub")

fun Application.opsHub(): OpsHub =
    attributes.computeIfAbsent(OpsHubAttribute) {
        OpsHub(loadAppConfig())
    }

class OpsHub(val config: AppConfig) {
    val database = OpsDatabase(config.databasePath)
    val metricsCollector = SystemMetricsCollector(config)
    val notifier = Notifier(config.alerts)
    val serviceMonitor = ServiceMonitor(config)
    val inventoryCollector = OpsInventoryCollector()
    val managementService = ManagementService(config.management, database)
    val logService = LogService(config.management)
    val automationRunner = AutomationRunner(config, database, metricsCollector, notifier)

    private val started = AtomicBoolean(false)
    private val lastHealthStates = ConcurrentHashMap<String, HealthStatus>()
    private var backgroundJob: Job? = null

    fun start() {
        if (backgroundDisabled()) return
        if (!started.compareAndSet(false, true)) return
        database.insertEvent(EventSeverity.INFO, "ops-hub", "application started")
        val supervisor = SupervisorJob()
        backgroundJob = supervisor
        val scope = CoroutineScope(supervisor + Dispatchers.Default)
        scope.launch {
            delay(3_000)
            while (isActive) {
                runCatching { collectOnce() }
                delay(config.collectionIntervalSeconds * 1000)
            }
        }
        scope.launch {
            while (isActive) {
                runCatching { automationRunner.runPeriodicTasks() }
                delay(60_000)
            }
        }
    }

    fun stop() {
        if (!started.compareAndSet(true, false)) return
        database.insertEvent(EventSeverity.INFO, "ops-hub", "application stopped")
        backgroundJob?.cancel()
        backgroundJob = null
    }

    suspend fun collectOnce(): SystemSnapshot {
        val snapshot = metricsCollector.collect()
        database.insertMetric(snapshot, config.retentionHours)
        evaluateMetricThresholds(snapshot)

        val serviceResults = serviceMonitor.runAll()
        for (result in serviceResults) {
            database.upsertServiceCheck(result)
            evaluateServiceTransition(result)
        }
        return snapshot
    }

    fun currentSnapshot(): SystemSnapshot = metricsCollector.collect()

    fun healthResponse(): ApiHealthResponse {
        val services = database.latestServiceChecks()
        val down = services.count { it.status == HealthStatus.DOWN }
        val snapshot = currentSnapshot()
        val resourceIssues = buildList {
            snapshot.cpu.systemPercent
                ?.takeIf { it >= config.alerts.cpuCriticalPercent }
                ?.let { add("CPU $it%") }
            snapshot.memory.usedPercent
                ?.takeIf { it >= config.alerts.memoryCriticalPercent }
                ?.let { add("memory $it%") }
            snapshot.disks.forEach { disk ->
                disk.usedPercent
                    ?.takeIf { it >= config.alerts.diskCriticalPercent }
                    ?.let { add("disk ${disk.path} $it%") }
            }
        }
        val status = when {
            down > 0 -> HealthStatus.DEGRADED
            resourceIssues.isNotEmpty() -> HealthStatus.DEGRADED
            services.any { it.status == HealthStatus.UNKNOWN } -> HealthStatus.UNKNOWN
            else -> HealthStatus.UP
        }
        return ApiHealthResponse(
            status = status,
            app = config.appName,
            version = "1.0.0-SNAPSHOT",
            timestamp = System.currentTimeMillis(),
            message = when {
                down > 0 -> "$down service checks failing"
                resourceIssues.isNotEmpty() -> resourceIssues.joinToString(", ")
                else -> "ok"
            },
        )
    }

    fun dashboardSummary(): DashboardSummary {
        val current = currentSnapshot()
        return DashboardSummary(
            health = healthResponse(),
            current = current,
            services = database.latestServiceChecks(),
            events = database.recentEvents(40),
            automation = automationRunner.summary(),
            authEnabled = config.auth.basicEnabled,
        )
    }

    suspend fun inventorySnapshot(): OpsInventorySnapshot {
        val raw = inventoryCollector.collect()
        val decoratedSections = raw.sections.map(managementService::decorateSection)
        val managedSections = managementService.collectSections(decoratedSections)
        val snapshot = raw.copy(sections = (decoratedSections + managedSections).map(logService::decorateSection))
        recordInventoryProblems(snapshot.problems)
        return snapshot
    }

    suspend fun runManagementAction(
        request: ManagementActionRequest,
        auditContext: ManagementAuditContext = ManagementAuditContext(),
    ): ManagementActionResponse =
        managementService.runAction(request, auditContext)

    suspend fun systemdLogs(unit: String, lines: Int): LogReadResponse =
        logService.systemdLogs(unit, lines)

    private suspend fun evaluateServiceTransition(result: ServiceCheckResult) {
        val key = "service:${result.kind}:${result.name}"
        val previous = lastHealthStates.put(key, result.status)
        if (previous == result.status) return

        when (result.status) {
            HealthStatus.DOWN -> {
                val event = database.insertEvent(EventSeverity.CRITICAL, key, "${result.name} is down", result.message)
                notifier.sendEvent("Service down", event)
            }
            HealthStatus.UP -> {
                if (previous == HealthStatus.DOWN) {
                    val resolvedCount = database.resolveOpenEvents(key)
                    database.insertEvent(EventSeverity.INFO, key, "${result.name} recovered", result.message)
                    notifier.sendRecovery("Service recovered", key, resolvedCount, "${result.name}: ${result.message}")
                }
            }
            HealthStatus.DEGRADED,
            HealthStatus.UNKNOWN,
            -> Unit
        }
    }

    private suspend fun evaluateMetricThresholds(snapshot: SystemSnapshot) {
        val cpu = snapshot.cpu.systemPercent
        evaluateThreshold("cpu", cpu, config.alerts.cpuCriticalPercent, "CPU usage")

        val memory = snapshot.memory.usedPercent
        evaluateThreshold("memory", memory, config.alerts.memoryCriticalPercent, "Memory usage")

        for (disk in snapshot.disks) {
            evaluateThreshold("disk:${disk.path}", disk.usedPercent, config.alerts.diskCriticalPercent, "Disk ${disk.path}")
        }
    }

    private suspend fun evaluateThreshold(key: String, value: Double?, threshold: Double, label: String) {
        if (value == null) return
        val status = if (value >= threshold) HealthStatus.DOWN else HealthStatus.UP
        val previous = lastHealthStates.put("metric:$key", status)
        if (previous == status) return

        if (status == HealthStatus.DOWN) {
            val message = "$label is at $value% (threshold $threshold%)"
            val source = "metric:$key"
            val event = database.insertEvent(EventSeverity.CRITICAL, source, message)
            notifier.sendEvent("Resource threshold exceeded", event)
        } else if (previous == HealthStatus.DOWN) {
            val message = "$label recovered to $value%"
            val source = "metric:$key"
            val resolvedCount = database.resolveOpenEvents(source)
            database.insertEvent(EventSeverity.INFO, source, message)
            notifier.sendRecovery("Resource recovered", source, resolvedCount, message)
        }
    }

    private fun recordInventoryProblems(problems: List<InventoryProblem>) {
        val currentProblems = problems.associateBy { it.source }
        val knownOpenSources = listOf("inventory:systemd-service:", "inventory:systemd-timer:")
            .flatMap(database::openActionEventSources)
            .toSet()

        for (source in knownOpenSources - currentProblems.keys) {
            val resolved = database.resolveOpenEvents(source)
            if (resolved > 0) {
                database.insertEvent(EventSeverity.INFO, source, "Inventory issue recovered")
            }
        }

        for ((source, problem) in currentProblems) {
            if (source in knownOpenSources) continue
            database.insertEvent(problem.severity, source, problem.message, problem.detail)
        }
    }

    private fun backgroundDisabled(): Boolean =
        System.getProperty("OPS_DISABLE_BACKGROUND").equals("true", ignoreCase = true) ||
            System.getenv("OPS_DISABLE_BACKGROUND").equals("true", ignoreCase = true)
}
