package net.lateinint

class LogService(
    private val config: ManagementConfig,
    private val commandRunner: suspend (List<String>, Long) -> CommandOutput = { command, timeout ->
        runProcessCommand(command, timeout, maxOutputChars = 20_000)
    },
) {
    fun decorateSection(section: InventorySection): InventorySection =
        section.copy(items = section.items.map(::decorateItem))

    suspend fun systemdLogs(unit: String, lines: Int): LogReadResponse {
        val normalized = normalizeSystemdUnitName(unit)
        require(normalized in config.allowedSystemdUnits) { "systemd unit is not allowed: $normalized" }

        val safeLines = lines.coerceIn(1, 500)
        val result = commandRunner(
            listOf(
                "journalctl",
                "-u",
                normalized,
                "-n",
                safeLines.toString(),
                "--no-pager",
                "--output=short-iso",
            ),
            10,
        )
        return LogReadResponse(
            targetType = LogTargetType.SYSTEMD_UNIT,
            name = normalized,
            lines = safeLines,
            success = result.success,
            exitCode = result.exitCode,
            output = result.output,
            timestamp = System.currentTimeMillis(),
        )
    }

    suspend fun dockerLogs(container: String, lines: Int): LogReadResponse {
        val normalized = normalizeDockerContainerName(container)
        require(isDockerContainerAllowed(normalized)) { "docker container is not allowed: $normalized" }

        val safeLines = lines.coerceIn(1, 500)
        val result = commandRunner(
            listOf(
                "docker",
                "logs",
                "--tail",
                safeLines.toString(),
                "--timestamps",
                normalized,
            ),
            10,
        )
        return LogReadResponse(
            targetType = LogTargetType.DOCKER_CONTAINER,
            name = normalized,
            lines = safeLines,
            success = result.success,
            exitCode = result.exitCode,
            output = result.output,
            timestamp = System.currentTimeMillis(),
        )
    }

    private fun decorateItem(item: InventoryItem): InventoryItem {
        val logTarget = when {
            item.kind.isSystemdKind() -> {
                val normalized = normalizeSystemdUnitName(item.name)
                LogTargetType.SYSTEMD_UNIT.takeIf { normalized in config.allowedSystemdUnits }
            }
            item.kind.isDockerKind() -> {
                val normalized = runCatching { normalizeDockerContainerName(item.name) }.getOrNull()
                LogTargetType.DOCKER_CONTAINER.takeIf { normalized != null && isDockerContainerAllowed(normalized) }
            }
            else -> null
        }
        return if (logTarget == null || logTarget in item.logs) item else item.copy(logs = item.logs + logTarget)
    }

    private fun String.isSystemdKind(): Boolean =
        this == "managed-systemd-unit" || this == "systemd-service" || this == "systemd-timer"

    private fun String.isDockerKind(): Boolean =
        this == "managed-docker-container" || this == "docker"

    private fun normalizeDockerContainerName(value: String): String {
        val trimmed = value.trim()
        require(trimmed.isNotBlank()) { "docker container name is required" }
        require(dockerNameRegex.matches(trimmed)) { "invalid docker container name" }
        return trimmed
    }

    private fun isDockerContainerAllowed(name: String): Boolean =
        config.allowAllDockerContainers || name in config.allowedDockerContainers

    private companion object {
        private val dockerNameRegex = Regex("""[A-Za-z0-9][A-Za-z0-9_.-]*""")
    }
}
