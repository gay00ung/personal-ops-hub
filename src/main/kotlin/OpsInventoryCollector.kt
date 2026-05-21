package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

class OpsInventoryCollector(
    private val etcPath: Path = Path.of("/etc"),
    private val commandRunner: suspend (List<String>) -> CommandOutput = { command -> runReadOnlyCommand(command) },
) {
    suspend fun collect(): OpsInventorySnapshot {
        val sections = buildList {
            add(userCrontab())
            add(systemCrontab())
            add(cronD())
            add(periodicCronDirectories())
            add(systemdTimers())
            add(failedSystemdServices())
            add(failedSystemdTimers())
            add(runningSystemdServices())
            add(dockerContainers())
            add(listeningPorts())
        }
        return OpsInventorySnapshot(
            timestamp = System.currentTimeMillis(),
            sections = sections,
            problems = sections.flatMap(::problemsFromSection),
        )
    }

    private suspend fun userCrontab(): InventorySection {
        val result = commandRunner(listOf("crontab", "-l"))
        if (!result.success) {
            val message = when {
                result.output.contains("no crontab", ignoreCase = true) -> "no user crontab"
                result.exitCode == null -> result.output.ifBlank { "crontab command unavailable" }
                else -> result.output.ifBlank { "crontab exited with ${result.exitCode}" }
            }
            return InventorySection("USER_CRONTAB", "User crontab", "crontab -l", result.exitCode != null, message)
        }
        return InventorySection(
            key = "USER_CRONTAB",
            title = "User crontab",
            source = "crontab -l",
            available = true,
            items = parseCronLines(result.output, "user crontab", hasUserColumn = false),
        )
    }

    private fun systemCrontab(): InventorySection {
        val path = etcPath.resolve("crontab")
        if (!Files.isReadable(path)) {
            return InventorySection("SYSTEM_CRONTAB", "System crontab", path.absolutePathString(), false, "not readable")
        }
        val text = runCatching { Files.readString(path) }.getOrElse { error ->
            return InventorySection("SYSTEM_CRONTAB", "System crontab", path.absolutePathString(), false, error.message)
        }
        return InventorySection(
            key = "SYSTEM_CRONTAB",
            title = "System crontab",
            source = path.absolutePathString(),
            available = true,
            items = parseCronLines(text, path.absolutePathString(), hasUserColumn = true),
        )
    }

    private fun cronD(): InventorySection {
        val dir = etcPath.resolve("cron.d")
        if (!Files.isDirectory(dir) || !Files.isReadable(dir)) {
            return InventorySection("CRON_D", "Cron drop-ins", dir.absolutePathString(), false, "directory not readable")
        }
        val items = Files.list(dir).use { paths ->
            paths
                .filter { it.isRegularFile() && Files.isReadable(it) }
                .flatMap { path -> parseCronLines(Files.readString(path), path.absolutePathString(), hasUserColumn = true).stream() }
                .toList()
        }
        return InventorySection("CRON_D", "Cron drop-ins", dir.absolutePathString(), true, items = items)
    }

    private fun periodicCronDirectories(): InventorySection {
        val dirs = listOf("cron.hourly", "cron.daily", "cron.weekly", "cron.monthly")
            .map { etcPath.resolve(it) }
        val items = dirs.flatMap { dir ->
            if (!Files.isDirectory(dir) || !Files.isReadable(dir)) return@flatMap emptyList()
            Files.list(dir).use { paths ->
                paths
                    .filter { it.isRegularFile() }
                    .map { path ->
                        InventoryItem(
                            kind = "cron-script",
                            name = path.name,
                            status = if (path.isExecutable()) "executable" else "file",
                            schedule = dir.name.removePrefix("cron."),
                            detail = path.absolutePathString(),
                        )
                    }
                    .toList()
            }
        }
        return InventorySection("CRON_PERIODIC", "Periodic cron scripts", "/etc/cron.*", true, items = items)
    }

    private suspend fun systemdTimers(): InventorySection =
        commandSection(
            key = "SYSTEMD_TIMERS",
            title = "Systemd timers",
            source = "systemctl list-timers --all",
            command = listOf("systemctl", "list-timers", "--all", "--no-pager", "--plain", "--no-legend"),
            parser = ::parseSystemdTimers,
        )

    private suspend fun failedSystemdServices(): InventorySection =
        commandSection(
            key = "FAILED_SERVICES",
            title = "Failed services",
            source = "systemctl list-units --type=service --state=failed",
            command = listOf("systemctl", "list-units", "--type=service", "--state=failed", "--all", "--no-pager", "--plain", "--no-legend"),
            parser = { output -> parseSystemdUnits(output, "systemd-service") },
        )

    private suspend fun failedSystemdTimers(): InventorySection =
        commandSection(
            key = "FAILED_TIMERS",
            title = "Failed timers",
            source = "systemctl list-units --type=timer --state=failed",
            command = listOf("systemctl", "list-units", "--type=timer", "--state=failed", "--all", "--no-pager", "--plain", "--no-legend"),
            parser = { output -> parseSystemdUnits(output, "systemd-timer") },
        )

    private suspend fun runningSystemdServices(): InventorySection =
        commandSection(
            key = "RUNNING_SERVICES",
            title = "Running services",
            source = "systemctl list-units --type=service --state=running",
            command = listOf("systemctl", "list-units", "--type=service", "--state=running", "--all", "--no-pager", "--plain", "--no-legend"),
            parser = { output -> parseSystemdUnits(output, "systemd-service").take(80) },
        )

    private suspend fun dockerContainers(): InventorySection =
        commandSection(
            key = "DOCKER_CONTAINERS",
            title = "Docker containers",
            source = "docker ps --all",
            command = listOf("docker", "ps", "--all", "--format", "{{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"),
            parser = ::parseDockerContainers,
        )

    private suspend fun listeningPorts(): InventorySection {
        val section = commandSection(
            key = "LISTENING_PORTS",
            title = "Listening ports",
            source = "ss -tulpnH",
            command = listOf("ss", "-tulpnH"),
            parser = ::parseListeningPorts,
        )
        if (section.available) return section
        return commandSection(
            key = "LISTENING_PORTS",
            title = "Listening ports",
            source = "lsof -nP -iTCP -sTCP:LISTEN",
            command = listOf("lsof", "-nP", "-iTCP", "-sTCP:LISTEN"),
            parser = ::parseListeningPorts,
        )
    }

    private suspend fun commandSection(
        key: String,
        title: String,
        source: String,
        command: List<String>,
        parser: (String) -> List<InventoryItem>,
    ): InventorySection {
        val result = commandRunner(command)
        if (!result.success) {
            return InventorySection(
                key = key,
                title = title,
                source = source,
                available = result.exitCode != null,
                message = result.output.ifBlank { if (result.exitCode == null) "command unavailable" else "exit ${result.exitCode}" },
            )
        }
        return InventorySection(key, title, source, true, items = parser(result.output))
    }

    private fun parseCronLines(text: String, source: String, hasUserColumn: Boolean): List<InventoryItem> =
        text.lineSequence()
            .mapIndexedNotNull { index, rawLine ->
                val line = rawLine.trim()
                if (line.isBlank() || line.startsWith("#") || cronEnvRegex.matches(line)) return@mapIndexedNotNull null
                val parts = line.split(Regex("\\s+"))
                val minimumParts = if (line.startsWith("@")) 2 else if (hasUserColumn) 7 else 6
                if (parts.size < minimumParts) {
                    return@mapIndexedNotNull InventoryItem("cron", "line ${index + 1}", detail = source, raw = line)
                }

                val schedule = if (line.startsWith("@")) {
                    parts[0]
                } else {
                    parts.take(5).joinToString(" ")
                }
                val commandIndex = when {
                    line.startsWith("@") && hasUserColumn -> 2
                    line.startsWith("@") -> 1
                    hasUserColumn -> 6
                    else -> 5
                }
                val user = if (!line.startsWith("@") && hasUserColumn) parts.getOrNull(5) else null
                val command = parts.drop(commandIndex).joinToString(" ")
                InventoryItem(
                    kind = "cron",
                    name = command.take(72).ifBlank { "line ${index + 1}" },
                    schedule = schedule,
                    command = command,
                    detail = listOfNotNull(source, user?.let { "user: $it" }).joinToString(" · "),
                    raw = line,
                )
            }
            .toList()

    private fun parseSystemdTimers(output: String): List<InventoryItem> =
        output.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.endsWith("timers listed.") }
            .map { line ->
                val parts = line.split(Regex("\\s{2,}")).map { it.trim() }
                if (parts.size >= 6) {
                    InventoryItem(
                        kind = "systemd-timer",
                        name = parts[4],
                        status = parts[1],
                        schedule = parts[0],
                        detail = "last ${parts[2]} · activates ${parts[5]}",
                        raw = line,
                    )
                } else {
                    InventoryItem("systemd-timer", line.substringBefore(' '), raw = line)
                }
            }

    private fun parseSystemdUnits(output: String, kind: String): List<InventoryItem> =
        output.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.endsWith("units listed.") }
            .map { line ->
                val parts = line.split(Regex("\\s+"), limit = 5)
                InventoryItem(
                    kind = kind,
                    name = parts.getOrElse(0) { line },
                    status = listOfNotNull(parts.getOrNull(2), parts.getOrNull(3)).joinToString("/").ifBlank { null },
                    detail = parts.getOrNull(4),
                    raw = line,
                )
            }

    private fun parseDockerContainers(output: String): List<InventoryItem> =
        output.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split('\t')
                InventoryItem(
                    kind = "docker",
                    name = parts.getOrElse(0) { line },
                    status = parts.getOrNull(2),
                    detail = parts.getOrNull(1),
                    command = parts.getOrNull(3),
                    raw = line,
                )
            }

    private fun parseListeningPorts(output: String): List<InventoryItem> =
        output.lines()
            .dropWhile { it.startsWith("COMMAND") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(120)
            .map { line ->
                val parts = line.split(Regex("\\s+"))
                val address = parts.firstOrNull { it.contains(':') || it.contains('*') }.orEmpty()
                InventoryItem(
                    kind = "port",
                    name = address.ifBlank { parts.getOrElse(0) { "listener" } },
                    status = parts.getOrNull(0),
                    detail = line.take(240),
                    raw = line,
                )
            }

    private fun problemsFromSection(section: InventorySection): List<InventoryProblem> =
        when (section.key) {
            "FAILED_SERVICES" -> section.items.map { item ->
                InventoryProblem(
                    severity = EventSeverity.WARNING,
                    source = "inventory:systemd-service:${item.name}",
                    message = "Systemd service ${item.name} is failed",
                    detail = item.detail ?: item.raw,
                )
            }
            "FAILED_TIMERS" -> section.items.map { item ->
                InventoryProblem(
                    severity = EventSeverity.WARNING,
                    source = "inventory:systemd-timer:${item.name}",
                    message = "Systemd timer ${item.name} is failed",
                    detail = item.detail ?: item.raw,
                )
            }
            else -> emptyList()
        }

    private companion object {
        private val cronEnvRegex = Regex("""[A-Za-z_][A-Za-z0-9_]*\s*=.*""")
    }
}

data class CommandOutput(
    val success: Boolean,
    val exitCode: Int?,
    val output: String,
)

private suspend fun runReadOnlyCommand(command: List<String>): CommandOutput =
    withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(3, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return@withContext CommandOutput(false, null, "timed out")
            }
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            CommandOutput(process.exitValue() == 0, process.exitValue(), output)
        }.getOrElse { error ->
            CommandOutput(false, null, error.message.orEmpty())
        }
    }
