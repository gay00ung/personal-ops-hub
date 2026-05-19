package net.lateinint

import com.sun.net.httpserver.HttpServer
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import java.util.Comparator
import kotlin.test.*

class ServerTest {
    private var tempDir: Path? = null

    @BeforeTest
    fun disableBackgroundJobs() {
        System.setProperty("OPS_DISABLE_BACKGROUND", "true")
        System.clearProperty("OPS_DISCORD_WEBHOOK_URL")
        System.clearProperty("OPS_TELEGRAM_BOT_TOKEN")
        System.clearProperty("OPS_TELEGRAM_CHAT_ID")
        tempDir = Files.createTempDirectory("ops-hub-test")
        System.setProperty("OPS_DB_PATH", tempDir!!.resolve("ops-hub.db").toString())
    }

    @AfterTest
    fun restoreBackgroundJobs() {
        System.clearProperty("OPS_DISABLE_BACKGROUND")
        System.clearProperty("OPS_DB_PATH")
        System.clearProperty("OPS_DISCORD_WEBHOOK_URL")
        System.clearProperty("OPS_TELEGRAM_BOT_TOKEN")
        System.clearProperty("OPS_TELEGRAM_CHAT_ID")
        tempDir?.let { dir ->
            Files.walk(dir).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }
        }
        tempDir = null
    }

    @Test
    fun `test root endpoint`() = testApplication {
        // loads default configuration
        configure()
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `health endpoint returns app status`() = testApplication {
        configure()

        val response = client.get("/api/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Personal Ops Hub")
    }

    @Test
    fun `dashboard endpoint serves the app shell`() = testApplication {
        configure()

        val response = client.get("/dashboard")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(body, "Personal Ops Hub")
        assertContains(body, "KR")
        assertContains(body, "EN")
    }

    @Test
    fun `summary endpoint includes metrics and automation`() = testApplication {
        configure()

        val response = client.get("/api/summary")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(body, "automation")
        assertContains(body, "current")
    }

    @Test
    fun `events can be filtered and acknowledged`() = testApplication {
        configure()

        val created = client.post("/api/backups/report") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"app","success":false,"message":"Backup failed on app server"}""")
        }
        val id = Regex(""""id":(\d+)""").find(created.bodyAsText())!!.groupValues[1]

        val updated = client.post("/api/events/$id/state") {
            contentType(ContentType.Application.Json)
            setBody("""{"state":"ACKNOWLEDGED"}""")
        }
        val filtered = client.get("/api/events?state=ACKNOWLEDGED&q=backup")
        val open = client.get("/api/events?state=OPEN&q=backup")

        assertEquals(HttpStatusCode.OK, updated.status)
        assertContains(updated.bodyAsText(), """"state":"ACKNOWLEDGED"""")
        assertContains(updated.bodyAsText(), """"actionRequired":true""")
        assertContains(filtered.bodyAsText(), "Backup failed on app server")
        assertEquals("[]", open.bodyAsText())
    }

    @Test
    fun `info events are log records instead of open action items`() = testApplication {
        configure()

        val created = client.post("/api/alerts/test") {
            contentType(ContentType.Application.Json)
            setBody("""{"message":"Manual note"}""")
        }
        val open = client.get("/api/events?state=OPEN&q=manual")

        assertEquals(HttpStatusCode.OK, created.status)
        assertContains(created.bodyAsText(), """"state":"RESOLVED"""")
        assertContains(created.bodyAsText(), """"actionRequired":false""")
        assertEquals("[]", open.bodyAsText())
    }

    @Test
    fun `successful backup report resolves previous failure automatically`() = testApplication {
        configure()

        client.post("/api/backups/report") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"nightly","success":false,"message":"Nightly backup failed"}""")
        }
        client.post("/api/backups/report") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"nightly","success":true,"message":"Nightly backup succeeded"}""")
        }

        val resolved = client.get("/api/events?state=RESOLVED&q=Nightly%20backup%20failed")
        val open = client.get("/api/events?state=OPEN&q=Nightly%20backup%20failed")

        assertContains(resolved.bodyAsText(), "Nightly backup failed")
        assertContains(resolved.bodyAsText(), """"state":"RESOLVED"""")
        assertEquals("[]", open.bodyAsText())
    }

    @Test
    fun `discord webhook receives backup failure and recovery alerts`() {
        TestWebhookServer().use { webhook ->
            System.setProperty("OPS_DISCORD_WEBHOOK_URL", webhook.url)

            testApplication {
                configure()

                client.post("/api/backups/report") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"nightly","success":false,"message":"Nightly backup failed"}""")
                }
                client.post("/api/backups/report") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"nightly","success":true,"message":"Nightly backup succeeded"}""")
                }
            }

            val bodies = webhook.bodies()
            assertEquals(2, bodies.size)
            assertContains(bodies[0], "Backup failed")
            assertContains(bodies[0], "Nightly backup failed")
            assertContains(bodies[1], "Backup recovered")
            assertContains(bodies[1], "resolved 1 open event")
        }
    }

    @Test
    fun `successful backup without open incident does not send alert`() {
        TestWebhookServer().use { webhook ->
            System.setProperty("OPS_DISCORD_WEBHOOK_URL", webhook.url)

            testApplication {
                configure()

                val response = client.post("/api/backups/report") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"name":"nightly","success":true,"message":"Nightly backup succeeded"}""")
                }

                assertEquals(HttpStatusCode.OK, response.status)
                assertContains(response.bodyAsText(), """"actionRequired":false""")
            }

            assertEquals(emptyList(), webhook.bodies())
        }
    }

    @Test
    fun `alert delivery failure does not break event creation`() = testApplication {
        System.setProperty("OPS_DISCORD_WEBHOOK_URL", "not a uri")
        configure()

        val response = client.post("/api/backups/report") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"app","success":false,"message":"Backup failed while webhook is broken"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Backup failed while webhook is broken")
        assertContains(response.bodyAsText(), """"actionRequired":true""")
    }

    @Test
    fun `github webhook is unavailable until configured`() = testApplication {
        configure()

        val response = client.post("/webhook/github")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertContains(response.bodyAsText(), "OPS_GITHUB_WEBHOOK_SECRET")
    }

    private class TestWebhookServer : AutoCloseable {
        private val messages = Collections.synchronizedList(mutableListOf<String>())
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)

        val url: String
            get() = "http://127.0.0.1:${server.address.port}/webhook"

        init {
            server.createContext("/webhook") { exchange ->
                val body = exchange.requestBody.bufferedReader().use { it.readText() }
                messages += body
                exchange.sendResponseHeaders(204, -1)
                exchange.close()
            }
            server.start()
        }

        fun bodies(): List<String> = synchronized(messages) { messages.toList() }

        override fun close() {
            server.stop(0)
        }
    }
}
