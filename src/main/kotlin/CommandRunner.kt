package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

suspend fun runProcessCommand(
    command: List<String>,
    timeoutSeconds: Long,
    maxOutputChars: Int = 8_000,
): CommandOutput =
    withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                process.waitFor(1, TimeUnit.SECONDS)
                val output = process.inputStream.bufferedReader().use { it.readText() }.trim().takeLast(maxOutputChars)
                return@withContext CommandOutput(false, null, output.ifBlank { "timed out" })
            }
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim().takeLast(maxOutputChars)
            CommandOutput(process.exitValue() == 0, process.exitValue(), output)
        }.getOrElse { error ->
            CommandOutput(false, null, error.message.orEmpty())
        }
    }
