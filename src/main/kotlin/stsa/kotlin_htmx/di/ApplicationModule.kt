package stsa.kotlin_htmx.di

import org.koin.dsl.module
import stsa.kotlin_htmx.api.CounterStrikeApiClient
import stsa.kotlin_htmx.auth.AuthenticationService
import stsa.kotlin_htmx.controllers.ApiController
import stsa.kotlin_htmx.controllers.HealthController
import stsa.kotlin_htmx.controllers.WebController
import stsa.kotlin_htmx.models.*
import stsa.kotlin_htmx.repositories.*
import stsa.kotlin_htmx.services.*
import stsa.kotlin_htmx.util.ErrorHandler
import stsa.kotlin_htmx.util.InMemorySearchCache
import stsa.kotlin_htmx.util.SearchCache
import stsa.kotlin_htmx.util.XmlConverter

val applicationModule = module {
    // Repositories
    single { SkinRepository() }
    single { AgentRepository() }
    single { CrateRepository() }
    single { CsKeyRepository() }
    
    // Caches
    single<SearchCache<SearchQuery, SearchResult<Skin>>> { InMemorySearchCache() }
    single<SearchCache<SearchQuery, SearchResult<Agent>>> { InMemorySearchCache() }
    single<SearchCache<SearchQuery, SearchResult<Crate>>> { InMemorySearchCache() }
    single<SearchCache<SearchQuery, SearchResult<CsKey>>> { InMemorySearchCache() }
    
    // Services
    single { SkinService(get(), get()) }
    single { AgentService(get(), get()) }
    single { CrateService(get(), get()) }
    single { CsKeyService(get(), get()) }
    single { AuthenticationService() }
    single { XmlConverter() }
    single { ErrorHandler() }
    
    // API Client
    single { CounterStrikeApiClient(getProperty("lookup_api_key")) }
    
    // Data Sync Service
    single { DataSyncService(get(), get(), get(), get(), get()) }
    
    // Controllers
    single { ApiController(get(), get(), get(), get()) }
    single { WebController(get(), get(), get(), get()) }
    single { HealthController() }
}