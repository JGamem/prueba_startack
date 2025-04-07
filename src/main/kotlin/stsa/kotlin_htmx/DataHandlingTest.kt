package stsa.kotlin_htmx

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataHandlingTest {
    @Test
    fun shouldGenerateXml() = testApplication {
        environment {
            config = ApplicationConfig(
                lookupApiKey = "test_api_key",
                jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                dbUsername = "sa",
                dbPassword = ""
            )
        }
        
        application {
            module()
        }

        client.get("/xml").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(ContentType.Application.Xml.withParameter("charset", "UTF-8"), contentType())
            
            val xmlContent = bodyAsText()
            assertTrue(xmlContent.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        }
    }
}