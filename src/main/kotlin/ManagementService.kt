package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ManagementService(
    private val config: ManagementConfig,
    private val database: OpsDatabase,
    private val commandRunner: suspend (List<String>, Long) -> CommandOutput = { command, timeout ->
        runManagedCommand(command, timeout)
    },
) {
    suspend fun collectSections(visibleSections: List<InventorySection> = emptyList()): List<InventorySection> = buildList {
        if (config.allowedSystemdUnits.isNotEmpty()) add(systemdSection())
        if (config.allowedDockerContainers.isNotEmpty() && !config.allowAllDockerContainers) {
            dockerSection(visibleSections)?.let(::add)
        }
    }

    fun decorateSection(section: InventorySection): InventorySection {
        if (section.key != "DOCKER_CONTAINERS" || !config.enabled) return section
        return section.copy(
            items = section.items.map { item ->
                if (!isDockerContainerAllowed(item.name)) item else item.copy(
                    actions = allowedDockerActions(item.status.orEmpty(), commandSucceeded = true),
                )
            },
        )
    }

    suspend fun runAction(request: ManagementActionRequest): ManagementActionResponse {
        val command = commandFor(request)
        val result = commandRunner(command, 20)
        val normalizedName = normalizedName(request.targetType, request.name)
        val source = "management:${request.targetType.name.lowercase()}:$normalizedName"
        val message = "${request.targetType.label()} ${request.action.label()} $normalizedName ${if (result.success) "succeeded" else "failed"}"
        val event = database.insertEvent(
            severity = if (result.success) EventSeverity.INFO else EventSeverity.WARNING,
            source = source,
            message = message,
            details = result.output.takeIf { it.isNotBlank() },
            actionRequired = !result.success,
        )
        return ManagementActionResponse(
            targetType = request.targetType,
            name = normalizedName,
            action = request.action,
            success = result.success,
            exitCode = result.exitCode,
            output = result.output,
            event = event,
        )
    }

    private suspend fun systemdSection(): InventorySection {
        if (!config.enabled) {
            return InventorySection(
                key = "MANAGED_SYSTEMD_UNITS",
                title = "Managed systemd units",
                source = "OPS_ALLOWED_SYSTEMD_UNITS",
                available = false,
                message = "management disabled",
            )
        }

        val items = config.allowedSystemdUnits.map { unit ->
            val result = commandRunner(
                listOf(
                    "systemctl",
                    "show",
                    unit,
                    "--property=LoadState,ActiveState,SubState,Description",
                    "--value",
                    "--no-pager",
                ),
                5,
            )
            if (!result.success && result.exitCode == null) {
                return InventorySection(
                    key = "MANAGED_SYSTEMD_UNITS",
                    title = "Managed systemd units",
                    source = "systemctl show",
                    available = false,
                    message = result.output.ifBlank { "systemctl unavailable" },
                )
            }
            val lines = result.output.lines()
            val loadState = lines.getOrNull(0).orEmpty().ifBlank { "unknown" }
            val activeState = lines.getOrNull(1).orEmpty().ifBlank { "unknown" }
            val subState = lines.getOrNull(2).orEmpty().ifBlank { "unknown" }
            val description = lines.getOrNull(3).orEmpty()
            InventoryItem(
                kind = "managed-systemd-unit",
                name = unit,
                status = listOf(loadState, activeState, subState).joinToString("/"),
                detail = description.ifBlank { result.output.ifBlank { "no status" } },
                command = "systemctl start|stop|restart $unit",
                actions = allowedSystemdActions(unit, loadState, activeState),
            )
        }
        return InventorySection(
            key = "MANAGED_SYSTEMD_UNITS",
            title = "Managed systemd units",
            source = "OPS_ALLOWED_SYSTEMD_UNITS",
            available = true,
            items = items,
        )
    }

    private suspend fun dockerSection(visibleSections: List<InventorySection>): InventorySection? {
        if (!config.enabled) {
            return InventorySection(
                key = "MANAGED_DOCKER_CONTAINERS",
                title = "Managed Docker containers",
                source = "OPS_ALLOWED_DOCKER_CONTAINERS",
                available = false,
                message = "management disabled",
            )
        }

        val visibleDockerNames = visibleSections
            .firstOrNull { it.key == "DOCKER_CONTAINERS" }
            ?.items
            .orEmpty()
            .map { it.name }
            .toSet()

        val missingAllowedContainers = config.allowedDockerContainers.filter { it !in visibleDockerNames }
        if (missingAllowedContainers.isEmpty()) return null

        val items = missingAllowedContainers.map { container ->
            val result = commandRunner(
                listOf("docker", "inspect", "--format", "{{.Name}}\t{{.State.Status}}\t{{.Config.Image}}", container),
                5,
            )
            if (!result.success && result.exitCode == null) {
                return InventorySection(
                    key = "MANAGED_DOCKER_CONTAINERS",
                    title = "Managed Docker containers",
                    source = "docker inspect",
                    available = false,
                    message = result.output.ifBlank { "docker unavailable" },
                )
            }
            val parts = result.output.split('\t')
            val name = parts.getOrNull(0)?.removePrefix("/")?.ifBlank { container } ?: container
            val status = parts.getOrNull(1)?.ifBlank { if (result.success) "unknown" else "unavailable" }
                ?: if (result.success) "unknown" else "unavailable"
            val image = parts.getOrNull(2).orEmpty()
            InventoryItem(
                kind = "managed-docker-container",
                name = name,
                status = status,
                detail = image.ifBlank { result.output.ifBlank { "no status" } },
                command = "docker start|stop|restart $container",
                actions = allowedDockerActions(status, result.success),
            )
        }
        return InventorySection(
            key = "MANAGED_DOCKER_CONTAINERS",
            title = "Managed Docker containers",
            source = "OPS_ALLOWED_DOCKER_CONTAINERS",
            available = true,
            items = items,
        )
    }

    private fun commandFor(request: ManagementActionRequest): List<String> {
        if (!config.enabled) throw IllegalArgumentException("management is disabled")
        val name = normalizedName(request.targetType, request.name)
        return when (request.targetType) {
            ManagementTargetType.SYSTEMD_UNIT -> {
                if (name !in config.allowedSystemdUnits) throw IllegalArgumentException("systemd unit is not allowed: $name")
                if (request.action == ManagementAction.STOP && name in config.restartOnlySystemdUnits) {
                    throw IllegalArgumentException("stop is disabled for $name")
                }
                listOf("systemctl", request.action.commandName(), name)
            }
            ManagementTargetType.DOCKER_CONTAINER -> {
                if (!isDockerContainerAllowed(name)) throw IllegalArgumentException("docker container is not allowed: $name")
                listOf("docker", request.action.commandName(), name)
            }
        }
    }

    private fun normalizedName(targetType: ManagementTargetType, name: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "target name is required" }
        return when (targetType) {
            ManagementTargetType.SYSTEMD_UNIT -> normalizeSystemdUnitName(trimmed)
            ManagementTargetType.DOCKER_CONTAINER -> {
                require(dockerNameRegex.matches(trimmed)) { "invalid docker container name" }
                trimmed
            }
        }
    }

    private fun isDockerContainerAllowed(name: String): Boolean =
        config.allowAllDockerContainers || name in config.allowedDockerContainers

    private fun allowedSystemdActions(unit: String, loadState: String, activeState: String): List<ManagementAction> {
        if (!config.enabled || loadState == "not-found") return emptyList()
        if (unit in config.restartOnlySystemdUnits) return listOf(ManagementAction.RESTART)
        return if (activeState == "active") {
            listOf(ManagementAction.RESTART, ManagementAction.STOP)
        } else {
            listOf(ManagementAction.START, ManagementAction.RESTART)
        }
    }

    private fun allowedDockerActions(status: String, commandSucceeded: Boolean): List<ManagementAction> {
        if (!config.enabled || !commandSucceeded) return emptyList()
        return if (status.isRunningDockerStatus()) {
            listOf(ManagementAction.RESTART, ManagementAction.STOP)
        } else {
            listOf(ManagementAction.START, ManagementAction.RESTART)
        }
    }

    private fun String.isRunningDockerStatus(): Boolean {
        val value = trim().lowercase()
        return value == "running" || value.startsWith("up ")
    }

    private fun ManagementAction.commandName(): String =
        name.lowercase()

    private fun ManagementAction.label(): String =
        name.lowercase()

    private fun ManagementTargetType.label(): String =
        when (this) {
            ManagementTargetType.SYSTEMD_UNIT -> "systemd unit"
            ManagementTargetType.DOCKER_CONTAINER -> "docker container"
        }

    private companion object {
        private val dockerNameRegex = Regex("""[A-Za-z0-9][A-Za-z0-9_.-]*""")
    }
}

private suspend fun runManagedCommand(command: List<String>, timeoutSeconds: Long): CommandOutput =
    withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim().takeLast(8000)
            if (!completed) {
                process.destroyForcibly()
                return@withContext CommandOutput(false, null, output.ifBlank { "timed out" })
            }
            CommandOutput(process.exitValue() == 0, process.exitValue(), output)
        }.getOrElse { error ->
            CommandOutput(false, null, error.message.orEmpty())
        }
    }
