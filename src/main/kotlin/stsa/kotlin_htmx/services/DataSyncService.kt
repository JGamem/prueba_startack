package stsa.kotlin_htmx.services

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.api.CounterStrikeApiClient
import stsa.kotlin_htmx.repositories.AgentRepository
import stsa.kotlin_htmx.repositories.CrateRepository
import stsa.kotlin_htmx.repositories.CsKeyRepository
import stsa.kotlin_htmx.repositories.SkinRepository

class DataSyncService(
    private val apiClient: CounterStrikeApiClient,
    private val skinRepository: SkinRepository,
    private val agentRepository: AgentRepository,
    private val crateRepository: CrateRepository,
    private val csKeyRepository: CsKeyRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    suspend fun synchronizeData() {
        logger.info("Starting data synchronization")
        
        try {
            val data = apiClient.fetchAllData()
            
            // Clear existing data
            skinRepository.deleteAll()
            agentRepository.deleteAll()
            crateRepository.deleteAll()
            csKeyRepository.deleteAll()
            
            // Insert new data
            crateRepository.saveAll(data.crates)
            skinRepository.saveAll(data.skins)
            agentRepository.saveAll(data.agents)
            csKeyRepository.saveAll(data.keys)
            
            logger.info("Data synchronization completed successfully")
        } catch (e: Exception) {
            logger.error("Error during data synchronization: ${e.message}", e)
            throw e
        }
    }
    
    fun synchronizeDataBlocking() = runBlocking {
        synchronizeData()
    }
}