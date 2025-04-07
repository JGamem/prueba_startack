package stsa.kotlin_htmx.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.models.*

class CounterStrikeApiClient(private val apiKey: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
    private val mapper = jacksonObjectMapper()
    
    suspend fun fetchAllData(): CounterStrikeData = coroutineScope {
        logger.info("Fetching data from Counter-Strike API")
        
        val skinsDeferred = async { fetchSkins() }
        val agentsDeferred = async { fetchAgents() }
        val cratesDeferred = async { fetchCrates() }
        val keysDeferred = async { fetchKeys() }
        
        val skins = skinsDeferred.await()
        val agents = agentsDeferred.await()
        val crates = cratesDeferred.await()
        val keys = keysDeferred.await()
        
        logger.info("Fetched ${skins.size} skins, ${agents.size} agents, ${crates.size} crates, ${keys.size} keys")
        
        CounterStrikeData(
            skins = skins,
            agents = agents,
            crates = crates,
            keys = keys
        )
    }
    
    private suspend fun fetchSkins(): List<Skin> {
        return try {
            val response: JsonNode = client.get("https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/skins") {
                contentType(ContentType.Application.Json)
            }.body()
            
            parseItems(response, "skins") { node ->
                Skin(
                    id = node["id"].asText(),
                    name = node["name"].asText(),
                    description = node["description"]?.asText(),
                    image = node["image"]?.asText(),
                    team = node["team"]?.asText(),
                    crates = node["crates"]?.map { it.asText() }
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching skins: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchAgents(): List<Agent> {
        return try {
            val response: JsonNode = client.get("https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/agents") {
                contentType(ContentType.Application.Json)
            }.body()
            
            parseItems(response, "agents") { node ->
                Agent(
                    id = node["id"].asText(),
                    name = node["name"].asText(),
                    description = node["description"]?.asText(),
                    image = node["image"]?.asText(),
                    team = node["team"]?.asText()
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching agents: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchCrates(): List<Crate> {
        return try {
            val response: JsonNode = client.get("https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/crates") {
                contentType(ContentType.Application.Json)
            }.body()
            
            parseItems(response, "crates") { node ->
                Crate(
                    id = node["id"].asText(),
                    name = node["name"].asText(),
                    description = node["description"]?.asText(),
                    image = node["image"]?.asText()
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching crates: ${e.message}", e)
            emptyList()
        }
    }
    
    private suspend fun fetchKeys(): List<CsKey> {
        return try {
            val response: JsonNode = client.get("https://raw.githubusercontent.com/ByMykel/CSGO-API/main/public/api/keys") {
                contentType(ContentType.Application.Json)
            }.body()
            
            parseItems(response, "keys") { node ->
                CsKey(
                    id = node["id"].asText(),
                    name = node["name"].asText(),
                    description = node["description"]?.asText(),
                    image = node["image"]?.asText(),
                    crates = node["crates"]?.map { it.asText() }
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching keys: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun <T> parseItems(response: JsonNode, field: String, mapper: (JsonNode) -> T): List<T> {
        return response[field]?.map(mapper) ?: emptyList()
    }
}

data class CounterStrikeData(
    val skins: List<Skin>,
    val agents: List<Agent>,
    val crates: List<Crate>,
    val keys: List<CsKey>
)