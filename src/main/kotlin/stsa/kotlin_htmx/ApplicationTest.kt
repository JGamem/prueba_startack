package stsa.kotlin_htmx

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.Test
import stsa.kotlin_htmx.database.DatabaseConfig
import stsa.kotlin_htmx.database.DatabaseFactory
import kotlin.test.assertEquals

class ApplicationTest {
    
    @Test
    fun testRoot() = testApplication {
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
        
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        
        // Keys should require authentication
        client.get("/api/keys").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
}