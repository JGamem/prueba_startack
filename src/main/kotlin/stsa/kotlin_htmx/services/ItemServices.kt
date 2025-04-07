package stsa.kotlin_htmx.services

import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.models.*
import stsa.kotlin_htmx.repositories.*
import stsa.kotlin_htmx.util.SearchCache

abstract class GameItemService<T : GameItem>(
    private val repository: GameItemRepository<T>,
    private val cache: SearchCache<SearchQuery, SearchResult<T>>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    suspend fun getAll(): List<T> {
        return try {
            repository.getAll()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.error("Error fetching all items: ${e.message}", e)
            emptyList()
        }
    }
    
    suspend fun getById(id: String): T? {
        return try {
            repository.getById(id)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.error("Error fetching item by id $id: ${e.message}", e)
            null
        }
    }
    
    suspend fun search(query: SearchQuery): SearchResult<T> {
        return try {
            // Check cache first
            cache.get(query) ?: repository.search(query).also { result ->
                // Store in cache
                cache.put(query, result)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.error("Error searching items: ${e.message}", e)
            SearchResult(emptyList(), 0, query.page, query.pageSize)
        }
    }
}

class SkinService(
    repository: SkinRepository,
    cache: SearchCache<SearchQuery, SearchResult<Skin>>
) : GameItemService<Skin>(repository, cache)

class AgentService(
    repository: AgentRepository,
    cache: SearchCache<SearchQuery, SearchResult<Agent>>
) : GameItemService<Agent>(repository, cache)

class CrateService(
    repository: CrateRepository,
    cache: SearchCache<SearchQuery, SearchResult<Crate>>
) : GameItemService<Crate>(repository, cache)

class CsKeyService(
    repository: CsKeyRepository,
    cache: SearchCache<SearchQuery, SearchResult<CsKey>>
) : GameItemService<CsKey>(repository, cache)