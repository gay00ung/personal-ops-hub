package net.lateinint

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.slf4j.LoggerFactory

class Notifier(private val config: AlertConfig) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    fun configuredTargets(): List<String> = buildList {
        if (!config.discordWebhookUrl.isNullOrBlank()) add("discord")
        if (!config.telegramBotToken.isNullOrBlank() && !config.telegramChatId.isNullOrBlank()) add("telegram")
    }

    suspend fun send(title: String, body: String) {
        val text = "[$title] $body"
        sendDiscord(text)
        sendTelegram(text)
    }

    suspend fun sendEvent(title: String, event: EventRecord) {
        if (!event.actionRequired) return
        send(title, event.alertBody())
    }

    suspend fun sendRecovery(title: String, source: String, resolvedCount: Int, body: String? = null) {
        if (resolvedCount <= 0) return
        val message = buildString {
            body?.trim()?.takeIf { it.isNotEmpty() }?.let {
                append(it)
                append('\n')
            }
            append("resolved ")
            append(resolvedCount)
            append(if (resolvedCount == 1) " open event" else " open events")
            append(" for ")
            append(source)
        }
        send(title, message)
    }

    private suspend fun sendDiscord(text: String) {
        val url = config.discordWebhookUrl ?: return
        val payload = AppJson.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            mapOf("content" to text.take(1900)),
        )
        postJson("discord", url, payload)
    }

    private suspend fun sendTelegram(text: String) {
        val token = config.telegramBotToken ?: return
        val chatId = config.telegramChatId ?: return
        val payload = AppJson.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            mapOf("chat_id" to chatId, "text" to text.take(3500)),
        )
        postJson("telegram", "https://api.telegram.org/bot$token/sendMessage", payload)
    }

    private suspend fun postJson(target: String, url: String, payload: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.discarding())
                if (response.statusCode() !in 200..299) {
                    logger.warn("{} alert failed with HTTP {}", target, response.statusCode())
                }
            }.onFailure { error ->
                logger.warn("{} alert failed: {}", target, error.message)
            }
        }
    }

    private fun EventRecord.alertBody(): String =
        buildString {
            append(severity)
            append(": ")
            append(message)
            append("\nsource: ")
            append(source)
            append("\nevent: #")
            append(id)
            details?.trim()?.takeIf { it.isNotEmpty() }?.let {
                append("\ndetails: ")
                append(it.take(800))
            }
        }

    private companion object {
        private val logger = LoggerFactory.getLogger(Notifier::class.java)
    }
}
