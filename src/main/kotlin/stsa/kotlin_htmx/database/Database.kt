package stsa.kotlin_htmx.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(config: DatabaseConfig) {
        val dataSource = hikari(config)
        val database = Database.connect(dataSource)
        
        // Create tables if they don't exist
        org.jetbrains.exposed.sql.transactions.transaction(database) {
            SchemaUtils.create(Skins, Agents, Crates, Keys, CrateReferences)
        }
    }

    private fun hikari(config: DatabaseConfig): DataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = config.jdbcUrl
        hikariConfig.username = config.username
        hikariConfig.password = config.password
        hikariConfig.maximumPoolSize = 3
        hikariConfig.isAutoCommit = false
        hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        hikariConfig.validate()
        return HikariDataSource(hikariConfig)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String
)