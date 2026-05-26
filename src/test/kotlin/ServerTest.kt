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
import kotlinx.coroutines.runBlocking
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
        System.clearProperty("OPS_MANAGE_ENABLED")
        System.clearProperty("OPS_ALLOWED_SYSTEMD_UNITS")
        System.clearProperty("OPS_RESTART_ONLY_SYSTEMD_UNITS")
        System.clearProperty("OPS_ALLOWED_DOCKER_CONTAINERS")
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
        System.clearProperty("OPS_MANAGE_ENABLED")
        System.clearProperty("OPS_ALLOWED_SYSTEMD_UNITS")
        System.clearProperty("OPS_RESTART_ONLY_SYSTEMD_UNITS")
        System.clearProperty("OPS_ALLOWED_DOCKER_CONTAINERS")
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
        assertContains(body, "Jobs")
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
    fun `inventory endpoint returns server job sections`() = testApplication {
        configure()

        val response = client.get("/api/inventory")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(body, """"sections"""")
        assertContains(body, "USER_CRONTAB")
        assertContains(body, "SYSTEMD_TIMERS")
    }

    @Test
    fun `inventory collector parses cron jobs and failed units`() = runBlocking {
        val etc = tempDir!!.resolve("etc")
        Files.createDirectories(etc.resolve("cron.d"))
        Files.createDirectories(etc.resolve("cron.daily"))
        Files.writeString(etc.resolve("crontab"), "0 2 * * * root /usr/local/bin/system-backup\n")
        Files.writeString(etc.resolve("cron.d").resolve("app"), "*/10 * * * * app /srv/app/heartbeat\n")
        Files.writeString(etc.resolve("cron.daily").resolve("cleanup"), "#!/bin/sh\n")

        val collector = OpsInventoryCollector(etc) { command ->
            when (command) {
                listOf("crontab", "-l") -> CommandOutput(true, 0, "*/5 * * * * /usr/local/bin/user-task")
                listOf("systemctl", "list-timers", "--all", "--no-pager", "--plain", "--no-legend") ->
                    CommandOutput(true, 0, "Wed 2026-05-20 09:00:00 KST  15h left  Tue 2026-05-19 09:00:00 KST  8h ago  backup.timer  backup.service")
                listOf("systemctl", "list-units", "--type=service", "--state=failed", "--all", "--no-pager", "--plain", "--no-legend") ->
                    CommandOutput(true, 0, "backup.service loaded failed failed Backup job")
                listOf("systemctl", "list-units", "--type=timer", "--state=failed", "--all", "--no-pager", "--plain", "--no-legend") ->
                    CommandOutput(true, 0, "")
                listOf("systemctl", "list-units", "--type=service", "--state=running", "--all", "--no-pager", "--plain", "--no-legend") ->
                    CommandOutput(true, 0, "ssh.service loaded active running OpenSSH server")
                listOf("docker", "ps", "--all", "--format", "{{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}") ->
                    CommandOutput(true, 0, "ops-hub\tpersonal-ops-hub:latest\tUp 1 hour\t0.0.0.0:8080->8080/tcp")
                listOf("ss", "-tulpnH") ->
                    CommandOutput(true, 0, "tcp LISTEN 0 4096 0.0.0.0:8080 0.0.0.0:*")
                else -> CommandOutput(false, null, "unexpected command: ${command.joinToString(" ")}")
            }
        }

        val snapshot = collector.collect()
        val commands = snapshot.sections.flatMap { it.items }.mapNotNull { it.command }

        assertTrue(commands.any { it.contains("/usr/local/bin/user-task") })
        assertTrue(commands.any { it.contains("/usr/local/bin/system-backup") })
        assertTrue(commands.any { it.contains("/srv/app/heartbeat") })
        assertTrue(snapshot.problems.any { it.source == "inventory:systemd-service:backup.service" })
    }

    @Test
    fun `management service only runs allowed actions and records events`() = runBlocking {
        val database = OpsDatabase(tempDir!!.resolve("management.db"))
        val commands = mutableListOf<List<String>>()
        val service = ManagementService(
            config = ManagementConfig(
                enabled = true,
                allowedSystemdUnits = listOf("demo.service", "personal-ops-hub.service"),
                restartOnlySystemdUnits = setOf("personal-ops-hub.service"),
                allowedDockerContainers = listOf("web"),
            ),
            database = database,
        ) { command, _ ->
            commands += command
            CommandOutput(true, 0, "ok")
        }

        val response = service.runAction(
            ManagementActionRequest(ManagementTargetType.SYSTEMD_UNIT, "demo", ManagementAction.RESTART),
        )

        assertTrue(response.success)
        assertEquals(listOf("systemctl", "restart", "demo.service"), commands.single())
        assertContains(database.recentEvents(query = "demo.service").single().message, "succeeded")
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                service.runAction(
                    ManagementActionRequest(ManagementTargetType.SYSTEMD_UNIT, "personal-ops-hub", ManagementAction.STOP),
                )
            }
        }
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                service.runAction(
                    ManagementActionRequest(ManagementTargetType.DOCKER_CONTAINER, "postgres", ManagementAction.RESTART),
                )
            }
        }
        Unit
    }

    @Test
    fun `management inventory exposes actions only for configured targets`() = runBlocking {
        val database = OpsDatabase(tempDir!!.resolve("management-inventory.db"))
        val service = ManagementService(
            config = ManagementConfig(
                enabled = true,
                allowedSystemdUnits = listOf("demo.service", "personal-ops-hub.service"),
                restartOnlySystemdUnits = setOf("personal-ops-hub.service"),
                allowedDockerContainers = listOf("web"),
            ),
            database = database,
        ) { command, _ ->
            when (command) {
                listOf(
                    "systemctl",
                    "show",
                    "demo.service",
                    "--property=LoadState,ActiveState,SubState,Description",
                    "--value",
                    "--no-pager",
                ) -> CommandOutput(true, 0, "loaded\nactive\nrunning\nDemo service")
                listOf(
                    "systemctl",
                    "show",
                    "personal-ops-hub.service",
                    "--property=LoadState,ActiveState,SubState,Description",
                    "--value",
                    "--no-pager",
                ) -> CommandOutput(true, 0, "loaded\nactive\nrunning\nPersonal Ops Hub")
                listOf("docker", "inspect", "--format", "{{.Name}}\t{{.State.Status}}\t{{.Config.Image}}", "web") ->
                    CommandOutput(true, 0, "/web\trunning\tnginx:latest")
                else -> CommandOutput(false, null, "unexpected command: ${command.joinToString(" ")}")
            }
        }

        val sections = service.collectSections()
        val systemdItems = sections.single { it.key == "MANAGED_SYSTEMD_UNITS" }.items
        val dockerItems = sections.single { it.key == "MANAGED_DOCKER_CONTAINERS" }.items

        assertEquals(listOf(ManagementAction.RESTART, ManagementAction.STOP), systemdItems.single { it.name == "demo.service" }.actions)
        assertEquals(listOf(ManagementAction.RESTART), systemdItems.single { it.name == "personal-ops-hub.service" }.actions)
        assertEquals(listOf(ManagementAction.RESTART, ManagementAction.STOP), dockerItems.single().actions)
    }

    @Test
    fun `management decorates visible docker inventory when wildcard is enabled`() = runBlocking {
        val database = OpsDatabase(tempDir!!.resolve("management-wildcard.db"))
        val commands = mutableListOf<List<String>>()
        val service = ManagementService(
            config = ManagementConfig(
                enabled = true,
                allowedSystemdUnits = emptyList(),
                restartOnlySystemdUnits = emptySet(),
                allowedDockerContainers = listOf("*"),
            ),
            database = database,
        ) { command, _ ->
            commands += command
            CommandOutput(true, 0, "ok")
        }

        val dockerSection = InventorySection(
            key = "DOCKER_CONTAINERS",
            title = "Docker containers",
            source = "docker ps --all",
            available = true,
            items = listOf(
                InventoryItem(kind = "docker", name = "web", status = "Up 3 weeks"),
                InventoryItem(kind = "docker", name = "postgres", status = "Exited (0) 2 hours ago"),
            ),
        )

        val decorated = service.decorateSection(dockerSection)
        val response = service.runAction(
            ManagementActionRequest(ManagementTargetType.DOCKER_CONTAINER, "postgres", ManagementAction.START),
        )

        assertEquals(listOf(ManagementAction.RESTART, ManagementAction.STOP), decorated.items.single { it.name == "web" }.actions)
        assertEquals(listOf(ManagementAction.START, ManagementAction.RESTART), decorated.items.single { it.name == "postgres" }.actions)
        assertEquals(listOf("docker", "start", "postgres"), commands.single())
        assertTrue(response.success)
    }

    @Test
    fun `log service reads only allowed systemd logs`() = runBlocking {
        val commands = mutableListOf<List<String>>()
        val service = LogService(
            config = ManagementConfig(
                enabled = false,
                allowedSystemdUnits = listOf("demo.service"),
                restartOnlySystemdUnits = emptySet(),
                allowedDockerContainers = emptyList(),
            ),
        ) { command, _ ->
            commands += command
            CommandOutput(true, 0, "2026-05-26T09:00:00+09:00 demo started")
        }

        val response = service.systemdLogs("demo", 1_000)

        assertTrue(response.success)
        assertEquals(500, response.lines)
        assertEquals("demo.service", response.name)
        assertEquals(
            listOf("journalctl", "-u", "demo.service", "-n", "500", "--no-pager", "--output=short-iso"),
            commands.single(),
        )
        assertFailsWith<IllegalArgumentException> {
            runBlocking { service.systemdLogs("ssh", 100) }
        }
        Unit
    }

    @Test
    fun `log service decorates allowed systemd inventory rows`() {
        val service = LogService(
            config = ManagementConfig(
                enabled = false,
                allowedSystemdUnits = listOf("demo.service"),
                restartOnlySystemdUnits = emptySet(),
                allowedDockerContainers = emptyList(),
            ),
        )
        val section = InventorySection(
            key = "RUNNING_SERVICES",
            title = "Running services",
            source = "systemctl",
            available = true,
            items = listOf(
                InventoryItem(kind = "systemd-service", name = "demo.service"),
                InventoryItem(kind = "systemd-service", name = "ssh.service"),
            ),
        )

        val decorated = service.decorateSection(section)

        assertEquals(listOf(LogTargetType.SYSTEMD_UNIT), decorated.items.single { it.name == "demo.service" }.logs)
        assertEquals(emptyList(), decorated.items.single { it.name == "ssh.service" }.logs)
    }

    @Test
    fun `systemd logs endpoint rejects disallowed unit`() = testApplication {
        configure()

        val response = client.get("/api/logs/systemd?unit=ssh")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "systemd unit is not allowed")
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
