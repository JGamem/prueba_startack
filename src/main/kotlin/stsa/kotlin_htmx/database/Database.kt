package stsa.kotlin_htmx.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(config: DatabaseConfig) {
        try {
            logger.info("Initializing database connection: ${config.jdbcUrl}")
            val dataSource = hikari(config)
            val database = Database.connect(dataSource)
            
            TransactionManager.manager.defaultIsolationLevel = 
                Connection.TRANSACTION_READ_COMMITTED

            transaction(database) {
                logger.info("Creating database schema if not exists")
                SchemaUtils.create(Skins, Agents, Crates, Keys, CrateReferences)
                logger.info("Schema creation completed")
            }
            
            transaction(database) {
                exec("SELECT 1")
                logger.info("Database connection test successful")
            }
            
            logger.info("Database initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize database: ${e.message}", e)
            throw e
        }
    }

    private fun hikari(config: DatabaseConfig): DataSource {
        logger.info("Configuring HikariCP connection pool")
        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            
            maximumPoolSize = 10 
            minimumIdle = 2
            idleTimeout = 30000
            connectionTimeout = 30000
            maxLifetime = 2000000

            isAutoCommit = true
            connectionTestQuery = "SELECT 1"

            addDataSourceProperty("ApplicationName", "StartrackApp")
            addDataSourceProperty("reWriteBatchedInserts", "true")

            validationTimeout = TimeUnit.SECONDS.toMillis(3)
        }
        
        return HikariDataSource(hikariConfig)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { 
            try {
                block()
            } catch (e: Exception) {
                logger.error("Database query failed: ${e.message}", e)
                throw e
            }
        }
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String
)