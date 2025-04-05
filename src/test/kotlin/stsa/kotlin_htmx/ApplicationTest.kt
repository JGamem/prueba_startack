package stsa.kotlin_htmx

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/skins").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/agents").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/crates").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/keys").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
        //TODO: <-- YOUR CODE HERE -> Use an authenticated client
        client.get("/keys").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
