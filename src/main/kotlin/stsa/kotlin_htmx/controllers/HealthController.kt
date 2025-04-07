package stsa.kotlin_htmx.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class HealthController {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    fun registerRoutes(application: Application) {
        application.routing {
            get("/health") {
                try {
                    // Check database connection
                    val dbStatus = checkDatabaseConnection()
                    
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "status" to "UP",
                            "timestamp" to ZonedDateTime.now().toString(),
                            "database" to dbStatus
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Health check failed: ${e.message}", e)
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        mapOf(
                            "status" to "DOWN",
                            "timestamp" to ZonedDateTime.now().toString(),
                            "error" to e.message
                        )
                    )
                }
            }
        }
    }
    
    private suspend fun checkDatabaseConnection(): Map<String, Any> = try {
        newSuspendedTransaction(Dispatchers.IO) {
            try {
                val statement = connection.prepareStatement("SELECT 1")
                val result = statement.executeQuery()
                result.next()
                mapOf(
                    "status" to "UP",
                    "message" to "Database connection is healthy"
                )
            } catch (e: Exception) {
                logger.error("Database query failed: ${e.message}", e)
                throw e
            }
        }
    } catch (e: Exception) {
        logger.error("Database health check failed: ${e.message}", e)
        mapOf(
            "status" to "DOWN",
            "message" to "Database connection failed: ${e.message}"
        )
    }
}