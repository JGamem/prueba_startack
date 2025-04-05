package stsa.kotlin_htmx

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.junit.Test
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals

class DataHandlingTest {
    @Test
    fun shouldGenerateXml() = testApplication {
        application {
            module()
        }

        client.get("/xml").apply {
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            assertEquals(
                db.parse(InputSource(StringReader(this.body<String>()))).xmlEncoding, "UTF-8")
        }
    }
}