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

    private fun decorateItem(item: InventoryItem): InventoryItem {
        if (!item.kind.isSystemdKind()) return item
        val normalized = normalizeSystemdUnitName(item.name)
        if (normalized !in config.allowedSystemdUnits) return item
        return item.copy(logs = listOf(LogTargetType.SYSTEMD_UNIT))
    }

    private fun String.isSystemdKind(): Boolean =
        this == "managed-systemd-unit" || this == "systemd-service" || this == "systemd-timer"
}
