package stsa.kotlin_htmx

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataHandlingTest {
    @Test
    fun shouldGenerateXml() = testApplication {
        application {
            module()
        }

        client.get("/xml").apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = body<String>()
            assertTrue(body.startsWith("<?xml"))
        }
    }
}