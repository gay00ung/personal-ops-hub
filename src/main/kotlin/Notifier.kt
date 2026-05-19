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

    private suspend fun sendDiscord(text: String) {
        val url = config.discordWebhookUrl ?: return
        val payload = AppJson.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            mapOf("content" to text.take(1900)),
        )
        postJson(url, payload)
    }

    private suspend fun sendTelegram(text: String) {
        val token = config.telegramBotToken ?: return
        val chatId = config.telegramChatId ?: return
        val payload = AppJson.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            mapOf("chat_id" to chatId, "text" to text.take(3500)),
        )
        postJson("https://api.telegram.org/bot$token/sendMessage", payload)
    }

    private suspend fun postJson(url: String, payload: String) {
        withContext(Dispatchers.IO) {
            val request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build()
            runCatching {
                client.send(request, HttpResponse.BodyHandlers.discarding())
            }
        }
    }
}
