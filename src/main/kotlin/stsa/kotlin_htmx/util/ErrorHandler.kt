package stsa.kotlin_htmx.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class ErrorHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    fun configureStatusPages(application: Application) {
        application.install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.error("Unhandled error: ${cause.message}", cause)
                
                when (cause) {
                    is IllegalArgumentException -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (cause.message ?: "Bad request"))
                        )
                    }
                    is IllegalStateException -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (cause.message ?: "Internal server error"))
                        )
                    }
                    else -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "An unexpected error occurred")
                        )
                    }
                }
            }
            
            status(HttpStatusCode.NotFound) { call, _ ->
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "The requested resource was not found")
                )
            }
            
            status(HttpStatusCode.Unauthorized) { call, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Authentication required")
                )
            }
            
            status(HttpStatusCode.Forbidden) { call, _ ->
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Access denied")
                )
            }
        }
    }
}