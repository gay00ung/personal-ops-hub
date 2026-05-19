package net.lateinint

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @BeforeTest
    fun disableBackgroundJobs() {
        System.setProperty("OPS_DISABLE_BACKGROUND", "true")
    }

    @AfterTest
    fun restoreBackgroundJobs() {
        System.clearProperty("OPS_DISABLE_BACKGROUND")
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
    fun `github webhook is unavailable until configured`() = testApplication {
        configure()

        val response = client.post("/webhook/github")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertContains(response.bodyAsText(), "OPS_GITHUB_WEBHOOK_SECRET")
    }

}
