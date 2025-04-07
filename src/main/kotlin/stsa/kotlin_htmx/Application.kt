package stsa.kotlin_htmx

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.auth.configureAuthentication
import stsa.kotlin_htmx.controllers.ApiController
import stsa.kotlin_htmx.controllers.HealthController
import stsa.kotlin_htmx.controllers.WebController
import stsa.kotlin_htmx.database.DatabaseConfig
import stsa.kotlin_htmx.database.DatabaseFactory
import stsa.kotlin_htmx.di.applicationModule
import stsa.kotlin_htmx.plugins.configureHTTP
import stsa.kotlin_htmx.plugins.configureMonitoring
import stsa.kotlin_htmx.plugins.configureRouting
import stsa.kotlin_htmx.services.DataSyncService
import stsa.kotlin_htmx.util.ErrorHandler
import java.io.File

private val logger = LoggerFactory.getLogger("Application")

data class ApplicationConfig(
    val lookupApiKey: String,
    val jdbcUrl: String,
    val dbUsername: String,
    val dbPassword: String
) {
    companion object {
        fun load(): ApplicationConfig {
            fun Map<String, String>.envOrLookup(key: String): String {
                return System.getenv(key) ?: this[key] ?: 
                    throw IllegalArgumentException("Missing required environment variable: $key")
            }

            val envVars: Map<String, String> = envFile().let { envFile ->
                if (envFile.exists()) {
                    envFile.readLines()
                        .map { it.split("=", limit = 2) }
                        .filter { it.size == 2 }
                        .associate { it.first().trim() to it.last().trim() }
                } else emptyMap()
            }

            return ApplicationConfig(
                lookupApiKey = envVars.envOrLookup("LOOKUP_API_KEY"),
                jdbcUrl = envVars.envOrLookup("JDBC_URL"),
                dbUsername = envVars.envOrLookup("DB_USERNAME"),
                dbPassword = envVars.envOrLookup("DB_PASSWORD")
            )
        }
    }
}

fun envFile(): File {
    return listOf(".env.local", ".env.default").map { File(it) }.firstOrNull { it.exists() } 
        ?: throw IllegalStateException("No environment file found")
}

fun main() {
    if (File(".env.default").exists() && File(".env.default").readText().contains("KTOR_DEVELOPMENT=true")) {
        System.setProperty("io.ktor.development", "true")
    }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {

    val config = ApplicationConfig.load()

    install(Koin) {
        slf4jLogger(Level.INFO)
        properties(mapOf(
            "lookup_api_key" to config.lookupApiKey
        ))
        modules(applicationModule)
    }
    
    // Configure infrastructure
    configureHTTP()
    configureMonitoring()
    configureRouting()
    configureAuthentication()
    
    install(Compression)
    install(ContentNegotiation) {
        jackson {}
    }

    val apiController by inject<ApiController>()
    val webController by inject<WebController>()
    val healthController by inject<HealthController>()
    val dataSyncService by inject<DataSyncService>()
    val errorHandler by inject<ErrorHandler>()
    
    errorHandler.configureStatusPages(this)
    
    DatabaseFactory.init(
        DatabaseConfig(
            jdbcUrl = config.jdbcUrl,
            username = config.dbUsername,
            password = config.dbPassword
        )
    )
    
    apiController.registerRoutes(this)
    webController.registerRoutes(this)
    healthController.registerRoutes(this)
    
    launch {
        try {
            logger.info("Starting initial data synchronization...")
            dataSyncService.synchronizeData()
            logger.info("Initial data synchronization completed successfully")
        } catch (e: Exception) {
            logger.error("Error during initial data synchronization: ${e.message}", e)
        }
    }
}